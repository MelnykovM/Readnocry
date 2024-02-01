document.addEventListener("DOMContentLoaded", function() {
  var rightPanel = document.getElementById('menu-smartphone');
  var menuButton = document.getElementById('menuButton');

  menuButton.addEventListener('click', function() {
    rightPanel.classList.toggle('open');
  });
});
