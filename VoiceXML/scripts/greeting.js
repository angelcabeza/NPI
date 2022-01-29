function greetEng(){
    // Sacamos la hora actual del usuario
    var d = new Date();
    var time = d.getHours();

    // Según la hora que sea saludamos de una manera
    if (time < 12) {
        return "Good morning";
    }
    if (time < 19) {
        return"Good afternoon.";
    }
    if (time >= 19) {
        return "Good night";
    }
}

function greetEsp(){
    // Sacamos la hora actual del usuario
    var d = new Date();
    var time = d.getHours();

    // Según la hora que sea saludamos de una manera
    if (time < 12) {
        return "Buenos dias";
    }
    if (time < 19) {
        return"Buenas tardes";
    }
    if (time >= 19) {
        return "Buenas noches";
    }
}