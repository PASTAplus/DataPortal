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
        alert(fileName);
        uploadFile(file);
    }
});

// Async function to handle file upload
async function uploadFile(file) {
    const endpoint = `${thumbnailApi}\\${scope}\\${identifier}\\${revision}`;

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                // Set the Content-Type header to the file's MIME type
                'Content-Type': file.type,
                'Cookie': `edi-token=${ediToken};auth-token=${authToken}`,
            },
            // Send the raw file data as the body
            body: file,
        });

        if (response.ok) {
            // Note: The response from the placeholder endpoint won't actually contain the file.
            // This is just a simulation. A real server would handle this differently.
            const result = await response.json();
            alert('Upload Successful');
        } else {
            throw new Error('Upload failed with status ' + response.status);
        }
    } catch (error) {
        alert(`Upload Failed - Error: ${error.message}`);
        console.error('Error:', error);
    }
}
