const VALID_PACKAGE_ID_RX = /^[a-z-]{3,}\.\d+\.\d+$/;
const VALID_DOI_RX = /^(https?:\/\/doi\.org\/)?10\.\d{4,9}\/.+$/;
const VALID_URL_RX = /^(ftp|http|https):\/\/[^ "]+$/;
const VALID_YEAR_RX = /^(\d{4}$)/;
const VALID_MAX_32_CHAR_RX = /^.{0,32}$/;

$(document).ready(function () {
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
    const journalPubYearInput = $('#journal-pub-year');
    const articleAuthorShortInput = $('#article-author-short');
    const journalIssueInput = $('#journal-issue');
    const journalVolumeInput = $('#journal-volume');
    const articlePagesInput = $('#article-pages');
    let articleAuthorList = null;

    const packageId = document.querySelector('.parameters').dataset.packageId;

    // Prevent accidental click outside the dialog from closing the modal.
    citationsModal.modal({
        show: false,
        backdrop: 'static',
        keyboard: false,

    });

    const citationsDataTable = citationsTable.removeAttr('width').DataTable({
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
        dom: 'Plftipr',
        autoWidth: true,
        // columns: [
        // The Citation ID column is hidden with CSS.
        // null, null, null, null, null, null, null,
        // ],
        // Attempt at controlling column widths. Couldn't get it working, maybe due to old jQuery.
        // fixedColumns: true,
        // columns: [
        //   { width: 10, targets: 0 },
        //   { width: 10, targets: 1 },
        //   { width: 10, targets: 2 },
        //   { width: 10, targets: 3 },
        //   { width: 10, targets: 4 },
        //   { width: 10, targets: 5 },
        //   { width: 10, targets: 6 },
        // ],
        // scrollCollapse: true,
        // scrollY:        "30px",
        // scrollX:        true,
    });

    // Event handlers

    $(document).on('show.bs.modal', '.modal', function () {
        console.debug('show.bs.modal');
        validateForm();
    });

    citationsModal.on('shown.bs.modal', function (_event) {
        console.debug('shown.bs.modal');
        // Scroll to top of modal body. Unfortunately, this doesn't work in the show.bs.modal handler, so we scroll
        // here, which may cause the scrolling to be visible.
        $('.modal-body').scrollTop(0);
    });

    citationsModal.on('hidden.bs.modal', function (_event) {
        // console.debug('hidden.bs.modal');
    });

    // Buttons

    // Start creating a new citation
    newButton.on('click', function (_event) {
        // Clear the form. It could contain values from a previous create or update.
        citationsModal.find('form')[0].reset();
        // Disable the Delete button.
        deleteButton.addClass('hidden');
        $('.modal').css('z-index', 100000);
        citationsModal.modal('show');
    });

    // Delete a citation
    deleteButton.on('click', function (_event) {
        const isSure = confirm('Are you sure you want to delete this citation?');
        if (!isSure) {
            return;
        }
        const citationMap = getModalValues();
        deleteCitation(citationMap);
    });

    // Close modal without making any changes
    cancelButton.on('click', function (_event) {
        citationsModal.modal('hide');
    });

    // Create or update a citation on OK button click
    okButton.on('click', function (_event) {
        const citationMap = getModalValues();
        if (citationMap.journalCitationId === '') {
            createCitation(citationMap);
        } else {
            updateCitation(citationMap);
        }
    });

    // Open the modal on row click
    citationsTable.on('click', 'tbody td', function (_event) {
        const clickedRow = $(this).closest('tr');
        const rowArr = $(clickedRow).find('td');
        const citationMapFromRow = getRowValues(rowArr);
        myFetch('GET', `journal-citation-crud?journalCitationId=${citationMapFromRow.journalCitationId}`, null).then(responseMap => {
            setModalValues(responseMap);
            deleteButton.removeClass('hidden');
            $('.modal').css('z-index', 100000);
            citationsModal.modal('show');
        });
    });

    // If we have a PackageID, then we're adding a new citation, so we open the modal directly.
    if (packageId !== '') {
        newButton.click();
    }

    // CRUD operations

    function createCitation(citationMap) {
        showSpinner();
        myFetch('POST', 'journal-citation-crud', citationMap).then(responseMap => {
            // Could not dynamically add the new row to the DataTable, so we reload the page. If the page was originally
            // opened with a packageId, we clear the query params to prevent the modal from opening again.
            const currentUrl = new URL(window.location.href);
            currentUrl.search = '';
            window.location.replace(currentUrl.toString());
            // If we want the modal to reopen with the same packageId, we can just reload the page without clearing the
            // query params.
            // location.reload();
        }).catch(() => {
            hideSpinner();
        });
    }

    function updateCitation(citationMap) {
        const rowArr = getRowArr(citationMap.journalCitationId);
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

    $('.form-control').on('keyup', function (_event) {
        validateForm();
    });

    function validateForm() {
        const hasArticleDoi = Boolean(articleDoiInput.val());
        const hasArticleUrl = Boolean(articleUrlInput.val());

        const isPackageIdValid = updateValidationClasses(packageInput, VALID_PACKAGE_ID_RX, false);
        const isDoiValid = updateValidationClasses(articleDoiInput, VALID_DOI_RX);
        const isUrlValid = updateValidationClasses(articleUrlInput, VALID_URL_RX);
        const isYearValid = updateValidationClasses(journalPubYearInput, VALID_YEAR_RX);
        const isJournalIssueValid = updateValidationClasses(journalIssueInput, VALID_MAX_32_CHAR_RX);
        const isJournalVolumeValid = updateValidationClasses(journalVolumeInput, VALID_MAX_32_CHAR_RX);
        const isArticlePagesValid = updateValidationClasses(articlePagesInput, VALID_MAX_32_CHAR_RX);

        let isFormValid = isPackageIdValid && isDoiValid && isUrlValid && isYearValid && isJournalIssueValid && isJournalVolumeValid && isArticlePagesValid;
        if (!(hasArticleDoi || hasArticleUrl)) {
            isFormValid = false;
        }

        fillButton.prop('disabled', !isDoiValid);
        openButton.prop('disabled', !isUrlValid);
        okButton.prop('disabled', !isFormValid);
    }

    function updateValidationClasses(inputEl, validationRx, emptyIsValid = true) {
        let el = $(inputEl).closest('.control-group');
        const val = inputEl.val();

        if (val === '' && emptyIsValid) {
            el.removeClass('success error');
            return true;
        }

        const isValid = validationRx.test(val);

        if (isValid) {
            el.removeClass('error');
            el.addClass('success');
        } else {
            el.removeClass('success');
            el.addClass('error');
        }

        return isValid;
    }

    // Fill the modal article URL and titles with values fetched by DOI lookup.

    fillButton.on('click', function (_event) {
        let doi = getDoi();
        showFillSpinner();
        myFetch('POST', 'doi', {doi: doi}).then(responseMap => {
            setModalDoiValues(responseMap);
        }).catch(() => {
            // alert('error');
        }).finally(() => {
            hideFillSpinner();
            validateForm();
        });
    });

    openButton.on('click', function (_event) {
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
            const valueJson = valueMap === null ? null : JSON.stringify(valueMap);

            fetch(path, {
                method: method, headers: {
                    'Content-Type': 'application/json; charset=UTF-8',
                }, body: valueJson,
            }).then(response => {
                isOk = response.ok;
                status = response.status;
                return response.text();
            }).then(body => {
                if (isOk) {
                    json = JSON.parse(body);
                    resolve(json);
                } else {
                    if (status === 400) {
                        alert(body);
                    } else if ([401, 403].includes(status)) {
                        alert('Access Denied. Try logging in again.');
                    } else if (status === 404) {
                        alert('Article DOI Not Found');
                    } else {
                        console.error("Internal error:");
                        console.error({body});
                        alert('Internal error. See console for details');
                    }
                    reject(null);
                }
            })
                // Catch any errors from fetch that are not HTTP errors.
                .catch(error => {
                    console.error("Internal error:");
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
            journalCitationId: $('#citation-id').val(),
            packageId: packageInput.val(),
            relationType: $('#relation-type').val(),
            articleDoi: getDoi(),
            articleUrl: articleUrlInput.val(),
            articleTitle: articleTitleInput.val(),
            journalTitle: journalTitleInput.val(),
            journalPubYear: journalPubYearInput.val(),
            articleAuthorList: articleAuthorList,
            journalIssue: journalIssueInput.val(),
            journalVolume: journalVolumeInput.val(),
            articlePages: articlePagesInput.val(),
        };
    }

    function setModalValues(citationMap) {
        $('#citation-id').val(citationMap.journalCitationId);
        packageInput.val(citationMap.packageId);
        $('#relation-type').val(citationMap.relationType);
        articleDoiInput.val(citationMap.articleDoi);
        articleUrlInput.val(citationMap.articleUrl);
        articleTitleInput.val(citationMap.articleTitle);
        journalTitleInput.val(citationMap.journalTitle);
        journalPubYearInput.val(citationMap.journalPubYear);
        articleAuthorList = citationMap.articleAuthorList;
        articleAuthorShortInput.val(citationMap.shortArticleAuthorList);
        journalIssueInput.val(citationMap.journalIssue);
        journalVolumeInput.val(citationMap.journalVolume);
        articlePagesInput.val(citationMap.articlePages);
    }

    function setModalDoiValues(doiMap) {
        if (doiMap.hasOwnProperty('error')) {
            alert(doiMap.error);
        } else {
            try {
                articleUrlInput.val(doiMap.resource.primary.URL);
                articleTitleInput.val(doiMap.title);
                journalTitleInput.val(doiMap['container-title']);

                const dateArr = doiMap.created['date-parts'][0];
                journalPubYearInput.val(dateArr[0]);

                articleAuthorShortInput.val(getAuthorsAsCitation(doiMap.author));
                articleAuthorList = sequenceToInt(doiMap.author);

                journalIssueInput.val(doiMap.issue);
                journalVolumeInput.val(doiMap.volume);
                articlePagesInput.val(doiMap.page);

            } catch (TypeError) {
                alert('Received unexpected value');
            }
        }
    }

    function getRowArr(journalCitationId) {
        return getRow(journalCitationId).find('td');
    }

    function getRow(journalCitationId) {
        let tr_el = null;
        $('#citations-table td:first-child').filter(function () {
            if ($(this).text() === journalCitationId) {
                tr_el = $(this).closest('tr');
            }
        });
        return tr_el;
    }

    function addRow(citationMap) {
        citationsDataTable.row.add(
            [
                citationMap.journalCitationId,
                citationMap.packageId,
                citationMap.relationType,
                citationMap.doi,
                citationMap.url,
                citationMap.articleTitle,
                citationMap.journalTitle,
                citationMap.journalPubYear
            ]
        ).draw();
    }

    function removeRow(citationMap) {
        const tr_el = getRow(citationMap.journalCitationId);
        citationsDataTable.row(tr_el).remove().draw();
    }

    function getRowValues(rowArr) {
        return {
            journalCitationId: rowArr[0].textContent,
            packageId: rowArr[1].textContent,
            relationType: rowArr[2].textContent,
            articleDoi: rowArr[3].textContent,
            articleUrl: rowArr[4].textContent,
            articleTitle: rowArr[5].textContent,
            journalTitle: rowArr[6].textContent,
            journalPubYear: rowArr[7].textContent,
        };
    }

    function setRowValues(rowArr, citationMap) {
        rowArr[0].textContent = citationMap.journalCitationId;
        rowArr[1].textContent = citationMap.packageId;
        rowArr[2].textContent = citationMap.relationType;
        rowArr[3].textContent = citationMap.articleDoi;
        rowArr[4].textContent = citationMap.articleUrl;
        rowArr[5].textContent = citationMap.articleTitle;
        rowArr[6].textContent = citationMap.journalTitle;
        rowArr[7].textContent = citationMap.journalPubYear;
    }

    function getAuthorsAsCitation(authorObj) {
        const authorList = [];

        authorObj.forEach(row => {
            const familyStr = row.family;
            const initialsStr = givenNameToInitials(row.given);
            const suffixStr = row.suffix;

            if (familyStr && initialsStr && suffixStr) {
                authorList.push(`${familyStr}, ${initialsStr} ${suffixStr}`);
            } else if (familyStr && initialsStr) {
                authorList.push(`${familyStr}, ${initialsStr}`);
            } else if (initialsStr && suffixStr) {
                authorList.push(`${initialsStr} ${suffixStr}`);
            } else if (familyStr && suffixStr) {
                authorList.push(`${suffixStr} ${familyStr}`);
            }
        });

        if (authorList.length > 1) {
            authorList[authorList.length - 1] = `& ${authorList[authorList.length - 1]}`;
        }

        return authorList.join(', ');
    }

    function givenNameToInitials(givenStr) {
        if (givenStr !== null) {
            return givenStr
                .split(' ')
                .map(s => `${s[0]}.`)
                .join(' ');
        }
    }

    // Crossref returns an author sequence designator as string, which is inconvenient for sorting, so
    // we convert it to an integer.
    function sequenceToInt(authorMap) {
        for (let i = 0; i < authorMap.length; i++) {
            if (authorMap[i].sequence === 'first') {
                authorMap[i].sequence = 0;
            }
            else if (authorMap[i].sequence === 'additional') {
                authorMap[i].sequence = 1;
            }
            else {
                authorMap[i].sequence = 2;
            }
        }
        return authorMap;
    }

    function getDoi() {
        return articleDoiInput.val().replace(/https?:\/\/doi\.org\//, '');
    }
});
