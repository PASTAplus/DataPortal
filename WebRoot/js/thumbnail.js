const thumbnailData = document.getElementById('thumbnail-data');
const thumbnailApi =  thumbnailData.dataset.thumbnailApi;
const ediToken = thumbnailData.dataset.ediToken;
const authToken = thumbnailData.dataset.authToken;
const scope = thumbnailData.dataset.scope;
const identifier = thumbnailData.dataset.identifier;
const revision = thumbnailData.dataset.revision;

const container = document.querySelector('.container');

document.addEventListener('click', event => {
    // Allow closest target to thumbnailTrigger
    const thumbnailTrigger = event.target.closest('.thumbnailTrigger');
    if (thumbnailTrigger) {
        let endpoint;
        const entityId = thumbnailTrigger.dataset.entityId;
        console.log('entityId', entityId);
        if (entityId) {
            endpoint = `${thumbnailApi}/${scope}/${identifier}/${revision}/${entityId}`;
        } else {
            endpoint = `${thumbnailApi}/${scope}/${identifier}/${revision}`;
        }
        console.log('endpoint', endpoint);
        thumbnailTrigger.addEventListener('click', () => {
            fileInput.click();
        });
        const thumbnailAction = thumbnailTrigger.dataset.thumbnailAction;
        console.log('thumbnailAction', thumbnailAction);
        if (thumbnailAction === 'add') {
            const fileInput = document.getElementById('thumbnailFileInput');
            fileInput.click();
            // Set up the change listener once, outside of the click handler
            fileInput.addEventListener('change', (event) => {
                const file = event.target.files[0];
                if (file) {
                    const fileName = file.name;
                    console.log('Upload fileName', fileName);
                    addThumbnail(file, endpoint);
                }
            }, { once: true }); // Use { once: true } to auto-remove listener
        } else {  // thumbnailAction === 'delete'
            const userConfirmed =  confirm('Continue deleting thumbnail image?');
            if (userConfirmed) {
                console.log('Deleting thumbnail');
                deleteThumbnail(endpoint);
            } else {
                console.log('Deleting thumbnail cancelled');
            }
        }

    }
});

async function addThumbnail(file, endpoint) {
    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            credentials: 'include',
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

async function deleteThumbnail(endpoint) {
    try {
        const response = await fetch(endpoint, {
            method: 'DELETE',
            credentials: 'include',
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

async function getSetCookie(endpoint) {
    try {
        const response = await fetch(endpoint, {
            method: 'OPTIONS',
            credentials: 'include',
            headers: {
                'X-New-Auth-Token': `edi-token=${ediToken};auth-token=${authToken}`,
            },
        });

        if (response.ok) {
            const result = await response.text();
            console.log(result);
        } else {
            throw new Error('getSetCookie failed with status ' + response.status);
        }
    } catch (error) {
        console.error('Error:', error);
    }
}
