const thumbnailData = document.getElementById('thumbnail-data');
const thumbnailApi =  thumbnailData.dataset.thumbnailApi;
const ediToken = thumbnailData.dataset.ediToken;
const authToken = thumbnailData.dataset.authToken;
const scope = thumbnailData.dataset.scope;
const identifier = thumbnailData.dataset.identifier;
const revision = thumbnailData.dataset.revision;

let entityId;
let endpoint;

const thumbnailTrigger = document.getElementById('thumbnailTrigger');

if (thumbnailTrigger) {
    let thumbnailAction = thumbnailTrigger.dataset.thumbnailAction;
    console.log(thumbnailAction);

    entityId = thumbnailTrigger.dataset.entityId;
    console.log(entityId);

    // Setup thumbnail API endpoint
    if (entityId) {
        endpoint = `${thumbnailApi}\\${scope}\\${identifier}\\${revision}\\${entityId}`;
    } else {
        endpoint = `${thumbnailApi}\\${scope}\\${identifier}\\${revision}`;
    }
    console.log(endpoint);

    if (thumbnailAction === 'add') {
        const fileInput = document.getElementById('fileInput');
        thumbnailTrigger.addEventListener('click', () => {
            fileInput.click();
        });
        fileInput.addEventListener('change', (event) => {
            const file = event.target.files[0];
            if (file) {
                const fileName = file.name
                uploadFile(file);
            }
        });
    } else {  // thumbnailAction === 'delete'
        thumbnailTrigger.addEventListener('click', () => {
            const userConfirmed =  confirm('Continue deleting thumbnail image?');
            if (userConfirmed) {
                console.log('Deleting thumbnail');
                deleteThumbnail();
            } else {
                console.log('Deleting thumbnail cancelled');
            }
        });
    }
}

async function uploadFile(file) {
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
            window.location.reload(true);
        } else {
            throw new Error('Upload failed with status ' + response.status);
        }
    } catch (error) {
        alert(`Upload Failed - Error: ${error.message}`);
        console.error('Error:', error);
    }
}

async function deleteThumbnail() {
    try {
        const response = await fetch(endpoint, {
            method: 'DELETE',
            headers: {
                'X-New-Auth-Token': `edi-token=${ediToken};auth-token=${authToken}`,
            },
        });

        if (response.ok) {
            const result = await response.text();
            console.log(result);
            window.location.reload(true);
        } else {
            throw new Error('Delete failed with status ' + response.status);
        }
    } catch (error) {
        alert(`Delete Failed - Error: ${error.message}`);
        console.error('Error:', error);
    }
}
