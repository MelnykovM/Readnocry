document.addEventListener("DOMContentLoaded", function() {
  var rightPanel = document.getElementById('menu-smartphone');
  var menuButton = document.getElementById('menuButton');
  menuButton.addEventListener('click', function() {
    rightPanel.classList.toggle('open');
  });
});

document.body.addEventListener('click', function(event) {
  if (event.target.id === 'sendMailButton') {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '/command/send-me-mail-again', true);
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          alert('Mail sent successfully!');
        } else {
          alert('Failed to send mail =(');
        }
      }
    };
    xhr.send();
  }
})

function confirmDelete(e) {
  if (!confirm("Are you sure you want to delete this book?")) {
    e.preventDefault();
  }
}
