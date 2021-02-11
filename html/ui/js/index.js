const nodeUrl = location.origin

async function getBRSVersion() {
    const url = `${nodeUrl}/?requestType=getState`
    const res = await fetch(url)

    if (!res.ok) {
        console.error('Request to peer failed', res.status)
        return ''
    }

    const {version} = await res.json()
    return version
}

(async () => {
    const version = await getBRSVersion()
    document.getElementById('version').textContent = version
})()
