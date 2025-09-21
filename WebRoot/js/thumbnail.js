const thumbnailData = document.getElementById('thumbnail-data');
const thumbnailApi =  thumbnailData.dataset.thumbnailApi;
const ediToken = thumbnailData.dataset.ediToken;
const authToken = thumbnailData.dataset.authToken;
const scope = thumbnailData.dataset.scope;
const identifier = thumbnailData.dataset.identifier;
const revision = thumbnailData.dataset.revision;
const entityId = thumbnailData.dataset.entityId;


const uploadTrigger = document.getElementById('thumbnailUploadTrigger');
const fileInput = document.getElementById('fileInput');

// Step 1: When the image is clicked, trigger the file input click event
uploadTrigger.addEventListener('click', () => {
    fileInput.click();
});

// Step 2: Listen for the 'change' event on the file input
fileInput.addEventListener('change', (event) => {
    const file = event.target.files[0];
    if (file) {
        const fileName = file.name
        uploadFile(file);
        window.location.reload(true);
    }
});

// Async function to handle file upload
async function uploadFile(file) {
    const endpoint = `${thumbnailApi}\\${scope}\\${identifier}\\${revision}`;

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'X-New-Auth-Token': `edi-token=${ediToken};auth-token=${authToken}`,
            },
            // Send the raw file data as the body
            body: file,
        });

        if (response.ok) {
            const result = await response.text();
            console.log(result);
        } else {
            throw new Error('Upload failed with status ' + response.status);
        }
    } catch (error) {
        alert(`Upload Failed - Error: ${error.message}`);
        console.error('Error:', error);
    }
}
