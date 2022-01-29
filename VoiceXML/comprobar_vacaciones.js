function isHolydays() {



	// el mensaje esta vacio inicialmente

	var mensaje = '';

	var fecha = new Date();



	var mes = 12;

	var dia = 25;

	var dia_semana = fecha.getDay();



	// si estamos en vacaciones de navidad, entre el 23 de diciembre y el 7 de enero

	// no esta disponible

	if ( (mes == 0 && dia < 8) || (mes == 11 && dia > 22) ) {

		mensaje = ' Lo siento, en esa fecha estamos cerrados debido a las vacaciones de Navidad. Inténtelo de nuevo en otra fecha';

	// si estamos en las vacaciones de verano también estamos cerrados

	} else if ( mes == 7 || mes == 8 && dia < 11 || mes == 6 && dia > 11 ) {

		mensaje = 'Lo siento, en esa fecha estamos cerrados debido a las vacaciones de Verano. Inténtelo de nuevo en otra fecha.';

	}



	return 'estamos cerrados';



}