$(document).ready(function () {
    const citedByTable = $('#cited-by-table');

    const citationsDataTable = citedByTable.DataTable({
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
    });
});
