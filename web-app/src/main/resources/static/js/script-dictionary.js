document.addEventListener("DOMContentLoaded", function() {
  var rightPanel = document.getElementById('menu-smartphone');
  var menuButton = document.getElementById('menuButton');
  menuButton.addEventListener('click', function() {
    rightPanel.classList.toggle('open');
  });
});

var stompClient = null;
function connect(username) {
  var socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/user/' + username + '/topic/dictionaryContent', function(message) {
      updateDictionary(message.body);
    });
    stompClient.subscribe('/user/' + username + '/topic/errorContent', function(message) {
      showError(message.body);
    });
    getDictionary();
  });
}

function showError(jsonData) {
  hideLoadingAnimation()
  var data = JSON.parse(jsonData);
  hideLoadingAnimation()
  var content = '<span>' + data.message + '</span>';
  document.getElementById('dictionary').innerHTML = '<div></div>';
  document.getElementById('errormessage').innerHTML = content;
}

function hideLoadingAnimation() {
  document.getElementById('loading').style.display = 'none';
}

function updateDictionary(jsonData) {
  hideLoadingAnimation()
  var content = '<div class="send-button-container">';
  var data = JSON.parse(jsonData);
  data.words.forEach(function(word) {
    content += '<div>' +
      '<button onclick="speakText(\'' + word.word + '\')"><i class="fas fa-volume-up"></i></button>' +
      '&nbsp;&nbsp;' +
      '<span>' + word.word + ' - ' + word.transcription + ' - ' + word.translation + '</span>' +
      '&nbsp;&nbsp;' +
      '<button onclick="deleteWord(\'' + word.wordId + '\', \'' + word.word + '\', \'' + word.transcription + '\', \'' + word.translation + '\', this)"><i class="fa fa-trash"></i></button>' +
      '</div>';
  });
  content += '</div>'
  document.getElementById('dictionary').innerHTML = content;
}

function deleteWord(wordId, word, transcription, translation, button) {
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  $.ajax({
    url: '/delete-from-dictionary',
    type: 'POST',
    headers: {
      [csrfHeader]: csrfToken
    },
    data: {
      wordId: wordId,
      word: word,
      transcription: transcription,
      translation: translation
    },
    success: function(response) {
      button.style.opacity = '0';
      button.style.pointerEvents = 'none';
    },
    error: function(error) {
      console.error("Ошибка при отправке слова", error);
    }
  });
}

function getDictionary() {
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  $.ajax({
    url: '/get-my-dictionary',
    type: 'GET',
    headers: {
      [csrfHeader]: csrfToken
    },
    error: function(error) {
      console.error("Error while sending get dictionary request", error);
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

var username = document.body.getAttribute('data-username');
connect(username);
