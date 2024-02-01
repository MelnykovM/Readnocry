function makeSentencesInteractive(text) {
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

  const sentences = splitIntoSentences(text);
  const bookContent = document.getElementById('bookContent');
  bookContent.innerHTML = '';

  var lastClickTime = 0;

  sentences.forEach((sentence, index) => {
    let sentenceElement = document.createElement('span');
    sentenceElement.textContent = sentence + ' ';
    sentenceElement.classList.add('sentence');

    sentenceElement.addEventListener('click', () => {
      const currentTime = new Date().getTime();
      if (currentTime - lastClickTime > 5000) {
        lastClickTime = currentTime;

        if (!stompClient || !stompClient.connected) {
          var username = document.body.getAttribute('data-username');
          connect(username);
        }
        console.log(`Sentence ${index + 1} was clicked.`);

        const requestData = {
          sentence: sentence,
          bookMetaDataId: bookMetaData.id
        };

        fetch('/translation/process-sentence', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              [csrfHeader]: csrfToken
            },
            body: JSON.stringify(requestData)
          })
          .then(response => response.text())
          .then(data => {
            console.log(data);
          })
          .catch(error => {
            console.error('Error:', error);
          });
      } else {
        console.log("Timeout ping");
      }
    });
    bookContent.appendChild(sentenceElement);
  });
}

function splitIntoSentences(text) {
  const abbreviations = ['Mr.', 'Mrs.', 'Ms.'];
  const parts = text.split(/([.!?])/);
  const sentences = [];

  for (let i = 0; i < parts.length; i += 2) {
    let sentence = parts[i].trim();
    if (i + 1 < parts.length) {
      sentence += parts[i + 1];
    }
    if (abbreviations.some(abbr => sentence.endsWith(abbr)) && i + 2 < parts.length) {
      parts[i + 2] = sentence + ' ' + parts[i + 2];
    } else {
      sentences.push(sentence);
    }
  }
  return sentences;
}

function updateCurrentPageDisplay(additionalPages) {
  var currentPageDisplay = document.getElementById('currentPageDisplay');
  var displayPageNumber = bookMetaData.page + additionalPages;
  currentPageDisplay.textContent = displayPageNumber;
}

function loadPage(page) {
  bookMetaData.page = page;
  $.get("/book/page", {
    bookMetaDataId: bookMetaData.id,
    page: bookMetaData.page
  }, function(data) {
    makeSentencesInteractive(data);
    updateCurrentPageDisplay(1)
  });
}

function nextPage() {
  var newPage = bookMetaData.page + 1;
  bookMetaData.page = newPage;
  $.get("/book/next-page", {
    bookMetaDataId: bookMetaData.id
  }, function(data) {
    makeSentencesInteractive(data);
    updateCurrentPageDisplay(1)
  });
}

function previousPage() {
  var newPage = bookMetaData.page - 1;
  if (newPage >= 0) {
    bookMetaData.page = newPage;
    $.get("/book/previous-page", {
      bookMetaDataId: bookMetaData.id
    }, function(data) {
      makeSentencesInteractive(data);
      updateCurrentPageDisplay(1)
    });
  }
}

$(document).ready(function() {
  console.log("Book was uploaded.");
  loadPage(bookMetaData.page);
  updateCurrentPageDisplay(1)
});

var stompClient = null;

function connect(username) {
  if (stompClient !== null && stompClient.connected) {
    return;
  }

  var socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/user/' + username + '/topic/translationContent', function(message) {
      updateTranslation(message.body);
    });
    stompClient.subscribe('/user/' + username + '/topic/control', function(message) {
      handleControlMessage(JSON.parse(message.body));
    });
  }, function(error) {
    console.log('Connection error: ' + error);
    setTimeout(reconnect, 5000);
  });
}

function reconnect() {
  if (stompClient !== null) {
    stompClient.disconnect(function() {
      console.log("Disconnected. Attempting to reconnect...");
      var username = document.body.getAttribute('data-username');
      connect(username);
    });
  } else {
    var username = document.body.getAttribute('data-username');
    connect(username);
  }
}

function handleControlMessage(message) {
  if (message.action === "showLoading") {
    showLoadingAnimation();
  }
}

function showLoadingAnimation() {
  document.getElementById('translation').innerHTML = '<div></div>';
  const currentTheme = localStorage.getItem('theme');

  if (currentTheme === 'light-theme') {
    document.getElementById('loading-light').style.display = 'block';
    document.getElementById('loading-dark').style.display = 'none';
  } else {
    document.getElementById('loading-light').style.display = 'none';
    document.getElementById('loading-dark').style.display = 'block';
  }
}

function hideLoadingAnimation() {
  document.getElementById('loading-light').style.display = 'none';
  document.getElementById('loading-dark').style.display = 'none';

}

function updateTranslation(jsonData) {
  hideLoadingAnimation()
  var data = JSON.parse(jsonData);
  var content = '<div class="send-button-container">';
  content += '<button onclick="speakText(\'' + data.sentence + '\')"><i class="fas fa-volume-up"></i></button>&nbsp;';
  content += '<span>' + data.sentence + '</span>';
  content += '<br>'
  content += '<br>'
  content += '<div class="line"></div>'
  content += '<p>' + data.translationResultDTO.translation1 + '</p>';
  content += '<p>' + data.translationResultDTO.translation2 + '</p>';
  content += '<div class="line"></div><br>'

  data.translationResultDTO.words.forEach(function(word) {
    content += '<div>' +
      '<button onclick="sendWord(\'' + word.word + '\', \'' + word.transcription + '\', \'' + word.translation + '\', this)">send</button>' +
      '&nbsp;<button onclick="speakText(\'' + word.word + '\')"><i class="fas fa-volume-up"></i></button>' +
      '<span>' + '     ' + word.word + ' - ' + word.transcription + ' - ' + word.translation + '</span>' +
      '</div>';
  });

  if (data.translationResultDTO.words.length == 0) {
    const currentTheme = localStorage.getItem('theme');
    if (currentTheme === 'light-theme') {
      content += '<div id="loading-light-second">'
      content += '<img src="/loading-light.gif" alt="Loading..." /></div>'
    } else {
      content += '<div id="loading-dark-second">'
      content += '<img src="/loading-dark.gif" alt="Loading..." /></div>'
    }
  }
  content += '</div>'
  document.getElementById('translation').innerHTML = content;
}

function sendWord(word, transcription, translation, button) {
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  $.ajax({
    url: '/add-to-dictionary',
    type: 'POST',
    headers: {
      [csrfHeader]: csrfToken
    },
    data: {
      word: word,
      transcription: transcription,
      translation: translation
    },
    success: function(response) {
      console.log("Word was sent");
      button.style.opacity = '0';
      button.style.pointerEvents = 'none';
    },
    error: function(error) {
      console.error("Error while word sending", error);
    }
  });
}

function speakText(text) {
  function trySpeak() {
    var voices = speechSynthesis.getVoices();
    var selectedVoice = voices.find(voice => voice.lang.startsWith('en') && voice.localService);
    if (!selectedVoice) {
      selectedVoice = voices.find(voice => voice.lang.startsWith('en'));
    }
    var speech = new SpeechSynthesisUtterance(text);
    speech.rate = 0.7;
    speech.pitch = 1;
    if (selectedVoice) {
      speech.voice = selectedVoice;
      console.log(`Using voice: ${selectedVoice.name}`);
    } else {
      console.log('Desired voice not found; using default.');
    }
    window.speechSynthesis.speak(speech);
  }
  if (speechSynthesis.getVoices().length === 0) {
    speechSynthesis.onvoiceschanged = trySpeak;
  } else {
    trySpeak();
  }
}

document.addEventListener("DOMContentLoaded", function() {
  var rightPanel = document.getElementById('menu-smartphone');
  var menuButton = document.getElementById('menuButton');
  menuButton.addEventListener('click', function() {
    rightPanel.classList.toggle('open');
  });
});

document.addEventListener("DOMContentLoaded", function() {
  var rightPanel = document.getElementById('right');
  var menuButton = document.getElementById('translation-menu-button');
  menuButton.addEventListener('click', function() {
    rightPanel.classList.toggle('open');
  });
});

document.addEventListener("DOMContentLoaded", function() {
  const body = document.body;
  const themeToggle = document.getElementById('themeToggle');
  const currentTheme = localStorage.getItem('theme');
  if (currentTheme) {
    body.classList.add(currentTheme);
    themeToggle.checked = currentTheme === 'light-theme';
  }

  themeToggle.addEventListener('click', () => {
    if (themeToggle.checked) {
      body.classList.replace('dark-theme', 'light-theme');
      localStorage.setItem('theme', 'light-theme');
    } else {
      body.classList.replace('light-theme', 'dark-theme');
      localStorage.setItem('theme', 'dark-theme');
    }
    window.location.reload();
  });
});

var username = document.body.getAttribute('data-username');
connect(username);
