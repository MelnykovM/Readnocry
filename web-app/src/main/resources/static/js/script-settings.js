document.addEventListener("DOMContentLoaded", function() {
  var rightPanel = document.getElementById('menu-smartphone');
  var menuButton = document.getElementById('menuButton');
  menuButton.addEventListener('click', function() {
    rightPanel.classList.toggle('open');
  });
});

function pageSizeEnum(value) {
  switch (parseInt(value)) {
    case 1200:
      return 'MIN';
    case 1600:
      return 'XS';
    case 2000:
      return 'S';
    case 2400:
      return 'M';
    case 2800:
      return 'L';
    case 3200:
      return 'XL';
    case 3600:
      return 'XXL';
    case 4000:
      return 'XXXL';
    case 4400:
      return 'MAX';
    default:
      return 'Unknown';
  }
}

document.getElementById("pageSizeSlider").oninput = function() {
  var pageSizeText = pageSizeEnum(this.value);
  document.getElementById("pageSizeValue").textContent = pageSizeText;
  document.getElementById("pageSizeEnum").value = pageSizeText;
}

document.addEventListener("DOMContentLoaded", function() {
  var slider = document.getElementById("pageSizeSlider");
  var pageSizeText = pageSizeEnum(slider.value);
  document.getElementById("pageSizeValue").textContent = pageSizeText;
  document.getElementById("pageSizeEnum").value = pageSizeText;
});

function languageProficiencyLabel(value) {
  switch (parseInt(value)) {
    case 1:
      return 'LOW (Beginner)';
    case 2:
      return 'MIDDLE (Pre-intermediate)';
    case 3:
      return 'HIGH (Intermediate)';
    default:
      return 'Unknown';
  }
}

document.getElementById("languageProficiencySlider").oninput = function() {
  var proficiencyText = languageProficiencyLabel(this.value);
  document.getElementById("languageProficiencyValue").textContent = proficiencyText;
  document.getElementById("languageProficiencyEnum").value = proficiencyText.split(' ')[0];
}

document.addEventListener("DOMContentLoaded", function() {
  var slider = document.getElementById("languageProficiencySlider");
  var proficiencyText = languageProficiencyLabel(slider.value);
  document.getElementById("languageProficiencyValue").textContent = proficiencyText;
  document.getElementById("languageProficiencyEnum").value = proficiencyText.split(' ')[0];
});
