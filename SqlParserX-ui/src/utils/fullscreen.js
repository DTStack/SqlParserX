export async function fullscreen(element) {
    if (element.requestFullscreen) {
        await element.requestFullscreen();
    } else if (element.webkitRequestFullscreen) {
        await element.webkitRequestFullscreen();
    } else if (element.msRequestFullscreen) {
        await element.msRequestFullscreen();
    }
}

export async function exitFullscreen() {
    if (document.exitFullscreen) {
        await document.exitFullscreen();
    } else if (document.webkitExitFullscreen) {
        await document.webkitExitFullscreen();
    } else if (document.msExitFullscreen) {
        await document.msExitFullscreen();
    }
}