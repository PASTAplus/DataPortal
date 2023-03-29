$(document).ready(function () {
  const citationsTable = $('#citations-table');
  const citationsModal = $('#citations-modal');

  // Prevent accidental click outside the dialog from closing the modal.
  // citationsModal.modal({
  //   backdrop: false,
  // });

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
    dom: 'Plftipr', autoWidth: false,
    "columns": [
      // The Citation ID column is hidden with CSS
      // {"visible": false},
      null,
      null,
      null,
      null,
      null,
      null,
      null,
    ]
  });

  // Event handlers

  $(document).on('show.bs.modal', '.modal', function () {
    console.debug('show.bs.modal');
  });

  citationsModal.on('hidden.bs.modal', function (_event) {
    console.debug('hidden.bs.modal');
  });

  // Start creating a new citation
  $('#new-button').on('click', function (_event) {
    console.debug('new-button');
    // Clear the form. It could contain values from a previous create or update.
    citationsModal.find('form')[0].reset()
    // Disable the Delete button.
    $('#delete-button').addClass('hidden');
    citationsModal.modal('show');
  });

  // Delete a citation
  $('#delete-button').on('click', function (_event) {
    const isSure = confirm("Are you sure you want to delete this citation?");
    if (!isSure) {
      return;
    }
    const citationMap = getModalValues();
    deleteCitation(citationMap);
  });

  // Open the modal on row click
  citationsTable.on('click', 'tbody td', function (_event) {
    console.debug('START open modal');
    const clickedRow = $(this).closest('tr');
    const rowArr = $(clickedRow).find('td');
    const citationMap = getRowValues(rowArr);
    setModalValues(citationMap);
    $('#delete-button').removeClass('hidden');
    citationsModal.modal('show');
    console.debug('END open modal');
  });

  // Close modal without making any changes
  citationsModal.find('#cancel-button').on('click', function (_event) {
    citationsModal.modal('hide');
  });

  // Create or update a citation on OK button click
  citationsModal.find('#ok-button').on('click', function (_event) {
    console.debug('ok-button');
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
    $('#new-button').click();
  }

  // CRUD operations

  function createCitation(citationMap)
  {
    showSpinner();
    myFetch('POST', citationMap)
    .then(responseMap => {
      location.reload();
      // citationMap.citationId = responseMap.citationId;
      // addRow(citationMap);
    })
    .catch(() => {
      hideSpinner();
    });
  }

  function updateCitation(citationMap)
  {
    const rowArr = getRowArr(citationMap.citationId);
    const oldCitationMap = getRowValues(rowArr);
    setRowValues(rowArr, citationMap)
    myFetch('PUT', citationMap)
    .catch(() => {
      setRowValues(rowArr, oldCitationMap)
    });
  }

  function deleteCitation(citationMap)
  {
    removeRow(citationMap);
    myFetch('DELETE', citationMap)
    .catch(() => {
      addRow(citationMap);
    });
  }

  // Helpers

  function myFetch(method, citationMap)
  {
    return new Promise((resolve, reject) => {
      let status = 0;
      let isOk = false;
      let json = null;

      fetch('journal-citation-crud', {
        method: method,
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(citationMap)
      })
      .then(response => {
        isOk = response.ok;
        status = response.status;
        return response.text();
      })
      .then(body => {
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
          else {
            console.error(body)
            alert(`Internal error (see console for details): ${body}`);
          }
          reject(null);
        }
      })
      // Catch any errors from fetch that are not HTTP errors.
      .catch(error => {
        console.error(error);
        alert(`Internal error (see console for details): ${error}`);
        reject(null);
      })
      .finally(() => {
      });
    });
  }

  function showSpinner()
  {
    $('#spinner-modal').modal('show');
  }

  function hideSpinner()
  {
    $('#spinner-modal').modal('hide');
  }

  function getModalValues()
  {
    return {
      citationId: $('#citation-id').val(),
      packageId: $('#package-id').val(),
      relationType: $('#relation-type').val(),
      doi: $('#article-doi').val(),
      url: $('#article-url').val(),
      articleTitle: $('#article-title').val(),
      journalTitle: $('#journal-title').val(),
    };
  }

  function setModalValues(citationMap)
  {
    $('#citation-id').val(citationMap.citationId);
    $('#package-id').val(citationMap.packageId);
    $('#relation-type').val(citationMap.relationType);
    $('#article-doi').val(citationMap.doi);
    $('#article-url').val(citationMap.url);
    $('#article-title').val(citationMap.articleTitle);
    $('#journal-title').val(citationMap.journalTitle);
  }

  function getRowArr(citationId)
  {
    return getRow(citationId).find('td');
  }

  function getRow(citationId)
  {
    let tr_el = null;
    $('#citations-table td:first-child').filter(function () {
      if ($(this).text() === citationId) {
        tr_el = $(this).closest('tr');
      }
    });
    return tr_el;
  }

  function addRow(citationMap)
  {
    citationsDataTable.row.add([
      citationMap.citationId,
      citationMap.packageId,
      citationMap.relationType,
      citationMap.doi,
      citationMap.url,
      citationMap.articleTitle,
      citationMap.journalTitle,
    ]).draw();
  }

  function removeRow(citationMap)
  {
    const tr_el = getRow(citationMap.citationId);
    citationsDataTable.row(tr_el).remove().draw();
  }

  function getRowValues(rowArr)
  {
    return {
      citationId: rowArr[0].textContent,
      packageId: rowArr[1].textContent,
      relationType: rowArr[2].textContent,
      doi: rowArr[3].textContent,
      url: rowArr[4].textContent,
      articleTitle: rowArr[5].textContent,
      journalTitle: rowArr[6].textContent,
    };
  }

  function setRowValues(rowArr, citationMap)
  {
    rowArr[0].textContent = citationMap.citationId;
    rowArr[1].textContent = citationMap.packageId;
    rowArr[2].textContent = citationMap.relationType;
    rowArr[3].textContent = citationMap.doi;
    rowArr[4].textContent = citationMap.url;
    rowArr[5].textContent = citationMap.articleTitle;
    rowArr[6].textContent = citationMap.journalTitle;
  }
})
;
