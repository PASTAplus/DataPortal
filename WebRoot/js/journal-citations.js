const VALID_DOI_RX = /^10\.\d{4,9}\/[-._;()/:A-Za-z\d]+$/;
const VALID_URL_RX = /^(ftp|http|https):\/\/[^ "]+$/;
const PACKAGE_ID_RX = /^[a-z-]{3,}\.\d+\.\d+$/;
$(document).ready(function() {
  const citationsTable = $('#citations-table');
  const citationsModal = $('#citations-modal');

  const newButton = $('#new-button');
  const deleteButton = $('#delete-button');
  const cancelButton = $('#cancel-button');
  const okButton = $('#ok-button');
  const fillButton = $('#fill-button');
  const openButton = $('#open-button');

  const packageInput = $('#package-id');
  const articleDoiInput = $('#article-doi');
  const articleUrlInput = $('#article-url');
  const articleTitleInput = $('#article-title');
  const journalTitleInput = $('#journal-title');

  // Prevent accidental click outside the dialog from closing the modal.
  citationsModal.modal({
    show: false, backdrop: 'static', keyboard: false,
  });

  const citationsDataTable = citationsTable.DataTable({
    // Enable DataTable features:
    // B - Buttons
    // R - ColReorder
    // S - Scroller
    // P - SearchPanes
    // Q - SearchBuilder
    // l - length changing input control
    // f - filtering input
    // t - The table!
    // i - Table information summary
    // p - pagination control
    // r - processing display element
    dom: 'Plftipr', autoWidth: false, 'columns': [// The Citation ID column is hidden with CSS
      // {"visible": false},
      null, null, null, null, null, null, null],
  });

  // Event handlers

  $(document).on('show.bs.modal', '.modal', function() {
    validateForm();
  });

  citationsModal.on('hidden.bs.modal', function(_event) {
  });

  // Start creating a new citation

  newButton.on('click', function(_event) {
    // Clear the form. It could contain values from a previous create or update.
    citationsModal.find('form')[0].reset();
    // Disable the Delete button.
    deleteButton.addClass('hidden');
    citationsModal.modal('show');
  });

  // Delete a citation
  deleteButton.on('click', function(_event) {
    const isSure = confirm('Are you sure you want to delete this citation?');
    if (!isSure) {
      return;
    }
    const citationMap = getModalValues();
    deleteCitation(citationMap);
  });

  // Open the modal on row click
  citationsTable.on('click', 'tbody td', function(_event) {
    const clickedRow = $(this).closest('tr');
    const rowArr = $(clickedRow).find('td');
    const citationMap = getRowValues(rowArr);
    setModalValues(citationMap);
    deleteButton.removeClass('hidden');
    citationsModal.modal('show');
  });

  // Close modal without making any changes
  cancelButton.on('click', function(_event) {
    citationsModal.modal('hide');
  });

  // Create or update a citation on OK button click
  okButton.on('click', function(_event) {
    const citationMap = getModalValues();
    if (citationMap.citationId === '') {
      createCitation(citationMap);
    }
    else {
      updateCitation(citationMap);
    }
  });

  // If we have a PackageID, then we're adding a new citation, so we open the modal directly.
  if (packageId !== '') {
    newButton.click();
  }

  // CRUD operations

  function createCitation(citationMap) {
    showSpinner();
    myFetch('POST', 'journal-citation-crud', citationMap).then(responseMap => {
      location.reload();
      // citationMap.citationId = responseMap.citationId;
      // addRow(citationMap);
    }).catch(() => {
      hideSpinner();
    });
  }

  function updateCitation(citationMap) {
    const rowArr = getRowArr(citationMap.citationId);
    const oldCitationMap = getRowValues(rowArr);
    setRowValues(rowArr, citationMap);
    myFetch('PUT', 'journal-citation-crud', citationMap).catch(() => {
      setRowValues(rowArr, oldCitationMap);
    });
  }

  function deleteCitation(citationMap) {
    removeRow(citationMap);
    myFetch('DELETE', 'journal-citation-crud', citationMap).catch(() => {
      addRow(citationMap);
    });
  }

  // Form validation

  $('.form-control').on('keyup', function(_event) {
    validateForm();
  });

  function validateForm() {
    const hasArticleDoi = Boolean(articleDoiInput.val());
    const hasArticleUrl = Boolean(articleUrlInput.val());

    const isPackageIdValid = PACKAGE_ID_RX.test(packageInput.val());
    const isDoiValid = !hasArticleDoi || VALID_DOI_RX.test(articleDoiInput.val());
    const isUrlValid = !hasArticleUrl || VALID_URL_RX.test(articleUrlInput.val());

    let formIsValid = isPackageIdValid && isDoiValid && isUrlValid;

    setValidationClasses(packageInput, isPackageIdValid);

    if (hasArticleDoi && hasArticleUrl) {
      setValidationClasses(articleDoiInput, isDoiValid);
      setValidationClasses(articleUrlInput, isUrlValid);
    }
    else if (hasArticleDoi && !hasArticleUrl) {
      setValidationClasses(articleDoiInput, isDoiValid);
      clearValidationClasses(articleUrlInput);
    }
    else if (!hasArticleDoi && hasArticleUrl) {
      clearValidationClasses(articleDoiInput);
      setValidationClasses(articleUrlInput, isUrlValid);
    }
    else {
      setValidationClasses(articleDoiInput, false);
      setValidationClasses(articleUrlInput, false);
      formIsValid = false;
    }

    okButton.prop('disabled', !formIsValid);
    fillButton.prop('disabled', !(hasArticleDoi && isDoiValid));
    openButton.prop('disabled', !(hasArticleUrl && isUrlValid));
  }

  function setValidationClasses(inputEl, isValid) {
    let el = $(inputEl).closest('.control-group');
    if (isValid) {
      el.removeClass('error');
      el.addClass('success');
    }
    else {
      el.removeClass('success');
      el.addClass('error');
    }
  }

  function clearValidationClasses(inputEl) {
    let el = $(inputEl).closest('.control-group');
    el.removeClass('success error');
  }

  // Fill the modal article URL and titles with values fetched by DOI lookup.

  fillButton.on('click', function(_event) {
    let doi = articleDoiInput.val();
    showFillSpinner();
    myFetch('POST', 'doi', { doi: doi }).then(responseMap => {
      setModalDoiValues(responseMap);
    }).catch(() => {
      // alert('error');
    }).finally(() => {
      hideFillSpinner();
      validateForm();
    });
  });

  openButton.on('click', function(_event) {
    window.open(articleUrlInput.val(), '_blank');
  });

  function showFillSpinner() {
    fillButton.prop('disabled', true);
    fillButton.addClass('spinner-border');
    fillButton.text('');
  }

  function hideFillSpinner() {
    fillButton.removeClass('spinner-border');
    fillButton.prop('disabled', false);
    fillButton.text('Fill ↲');
  }

  // Helpers

  function myFetch(method, path, valueMap = {}) {
    return new Promise((resolve, reject) => {
      let status = 0;
      let isOk = false;
      let json = null;

      fetch(path, {
        method: method, headers: {
          'Content-Type': 'application/json',
        }, body: JSON.stringify(valueMap),
      }).then(response => {
        isOk = response.ok;
        status = response.status;
        return response.text();
      }).then(body => {
        if (isOk) {
          json = JSON.parse(body);
          resolve(json);
        }
        else {
          if (status === 400) {
            alert(body);
          }
          else if ([401, 403].includes(status)) {
            alert('Access Denied. Try logging in again.');
          }
          else if (status === 404) {
            alert('Article DOI Not Found');
          }
          else {
            console.error(`Internal error: ${body}`);
            alert('Internal error. See console for details');
          }
          reject(null);
        }
      })
      // Catch any errors from fetch that are not HTTP errors.
      .catch(error => {
        console.error(error);
        alert('Internal error. See console for details');
        reject(null);
      }).finally(() => {
      });
    });
  }

  function showSpinner() {
    $('#spinner-modal').modal('show');
  }

  function hideSpinner() {
    $('#spinner-modal').modal('hide');
  }

  function getModalValues() {
    return {
      citationId: $('#citation-id').val(), packageId: packageInput.val(),
      relationType: $('#relation-type').val(), doi: articleDoiInput.val(),
      url: articleUrlInput.val(), articleTitle: articleTitleInput.val(),
      journalTitle: journalTitleInput.val(),
    };
  }

  function setModalValues(citationMap) {
    $('#citation-id').val(citationMap.citationId);
    packageInput.val(citationMap.packageId);
    $('#relation-type').val(citationMap.relationType);
    articleDoiInput.val(citationMap.doi);
    articleUrlInput.val(citationMap.url);
    articleTitleInput.val(citationMap.articleTitle);
    journalTitleInput.val(citationMap.journalTitle);
  }

  function setModalDoiValues(doiMap) {
    articleUrlInput.val(doiMap.resource.primary.URL);
    articleTitleInput.val(doiMap.title);
    journalTitleInput.val(doiMap['container-title']);
  }

  function getRowArr(citationId) {
    return getRow(citationId).find('td');
  }

  function getRow(citationId) {
    let tr_el = null;
    $('#citations-table td:first-child').filter(function() {
      if ($(this).text() === citationId) {
        tr_el = $(this).closest('tr');
      }
    });
    return tr_el;
  }

  function addRow(citationMap) {
    citationsDataTable.row.add(
      [citationMap.citationId, citationMap.packageId, citationMap.relationType, citationMap.doi, citationMap.url, citationMap.articleTitle, citationMap.journalTitle]).
    draw();
  }

  function removeRow(citationMap) {
    const tr_el = getRow(citationMap.citationId);
    citationsDataTable.row(tr_el).remove().draw();
  }

  function getRowValues(rowArr) {
    return {
      citationId: rowArr[0].textContent, packageId: rowArr[1].textContent,
      relationType: rowArr[2].textContent, doi: rowArr[3].textContent, url: rowArr[4].textContent,
      articleTitle: rowArr[5].textContent, journalTitle: rowArr[6].textContent,
    };
  }

  function setRowValues(rowArr, citationMap) {
    rowArr[0].textContent = citationMap.citationId;
    rowArr[1].textContent = citationMap.packageId;
    rowArr[2].textContent = citationMap.relationType;
    rowArr[3].textContent = citationMap.doi;
    rowArr[4].textContent = citationMap.url;
    rowArr[5].textContent = citationMap.articleTitle;
    rowArr[6].textContent = citationMap.journalTitle;
  }
});
