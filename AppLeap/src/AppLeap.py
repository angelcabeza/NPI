#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Nov  4 10:39:06 2021

@author: angel perro


    HELLO WORLD DE PYTHON:
https://developer-archive.leapmotion.com/documentation/v2/python/devguide/Sample_Tutorial.html?proglang=python

"""


import tkinter as tk
import os, sys, thread, time

# Insertar el path a las librerias
sys.path.insert(0, "../lib")

import Leap
from Leap import CircleGesture, KeyTapGesture, ScreenTapGesture, SwipeGesture


ANCHO_INTERFAZ = 1500
ALTO_INTERFAZ = 700
RADIO_PUNTERO = 20


path_puntero = "../imagenes/puntero.png"




class SampleListener(Leap.Listener):
    finger_names = ['Thumb', 'Index', 'Middle', 'Ring', 'Pinky']
    bone_names = ['Metacarpal', 'Proximal', 'Intermediate', 'Distal']
    state_names = ['STATE_INVALID', 'STATE_START', 'STATE_UPDATE', 'STATE_END']
    dedos_extendidos = False
    dedos_cerrados_tras_extendidos = False
    tiempo_ultimo_extendidos = 0

    def on_init(self, controller):
        print "Initialized"

    def on_connect(self, controller):
        print "Connected"

        # Enable gestures
        controller.enable_gesture(Leap.Gesture.TYPE_CIRCLE);
        controller.enable_gesture(Leap.Gesture.TYPE_KEY_TAP);
        controller.enable_gesture(Leap.Gesture.TYPE_SCREEN_TAP);
        controller.enable_gesture(Leap.Gesture.TYPE_SWIPE);

    def on_disconnect(self, controller):
        # Note: not dispatched when running in a debugger.
        print "Disconnected"

    def on_exit(self, controller):
        print "Exited"

    def on_frame(self, controller):
        # Get the most recent frame and report some basic information
        frame = controller.frame()

        print "Frame id: %d, timestamp: %d, hands: %d, fingers: %d, tools: %d, gestures: %d" % (
              frame.id, frame.timestamp, len(frame.hands), len(frame.fingers), len(frame.tools), len(frame.gestures()))

        # Cambiar puntero en pantalla
        pointable = frame.pointables.frontmost
        if pointable.is_valid:
            iBox = frame.interaction_box
            leapPoint = pointable.stabilized_tip_position
            normalizedPoint = iBox.normalize_point(leapPoint, False)
            
            app_x = normalizedPoint.x * ANCHO_INTERFAZ
            app_y = (1 - normalizedPoint.y) * ALTO_INTERFAZ
            
            actualizar_puntero_general(app_x, app_y)
            actualizar_puntero_sitios(app_x, app_y)
    
        # Get hands
        for hand in frame.hands:

            handType = "Left hand" if hand.is_left else "Right hand"

            print "  %s, id %d, position: %s" % (
                handType, hand.id, hand.palm_position)

            # Get the hand's normal vector and direction
            normal = hand.palm_normal
            direction = hand.direction

            # Calculate the hand's pitch, roll, and yaw angles
            print "  pitch: %f degrees, roll: %f degrees, yaw: %f degrees" % (
                direction.pitch * Leap.RAD_TO_DEG,
                normal.roll * Leap.RAD_TO_DEG,
                direction.yaw * Leap.RAD_TO_DEG)

            # Get arm bone
            arm = hand.arm
            print "  Arm direction: %s, wrist position: %s, elbow position: %s" % (
                arm.direction,
                arm.wrist_position,
                arm.elbow_position)


            extendedFingers = 0
            # Get fingers
            for finger in hand.fingers:

                print "    %s finger, id: %d, length: %fmm, width: %fmm" % (
                    self.finger_names[finger.type],
                    finger.id,
                    finger.length,
                    finger.width)
                
                if finger.extended:
                    extendedFingers = extendedFingers + 1
                

                # Get bones
                for b in range(0, 4):
                    bone = finger.bone(b)
                    print "      Bone: %s, start: %s, end: %s, direction: %s" % (
                        self.bone_names[bone.type],
                        bone.prev_joint,
                        bone.next_joint,
                        bone.direction)
            
            if extendedFingers == 5 and not self.dedos_cerrados_tras_extendidos:
                self.tiempo_ultimo_extendidos = time.time()
                self.dedos_extendidos = True
            elif extendedFingers == 0 and self.dedos_extendidos:
                self.dedos_cerrados_tras_extendidos = True
            elif extendedFingers == 5 and self.dedos_cerrados_tras_extendidos:
                tiempo_volver_abrir = time.time()
                tiempo_cerrados = tiempo_ultimo_extendidos - tiempo_volver_abrir
                usuario_click(tiempo_cerrados)
            else:
                self.dedos_extendidos = False
                self.dedos_cerrados_tras_extendidos = False

        # Get tools
        for tool in frame.tools:

            print "  Tool id: %d, position: %s, direction: %s" % (
                tool.id, tool.tip_position, tool.direction)

        # Get gestures
        for gesture in frame.gestures():
            if gesture.type == Leap.Gesture.TYPE_CIRCLE:
                circle = CircleGesture(gesture)

                # Determine clock direction using the angle between the pointable and the circle normal
                if circle.pointable.direction.angle_to(circle.normal) <= Leap.PI/2:
                    clockwiseness = "clockwise"
                else:
                    clockwiseness = "counterclockwise"

                # Calculate the angle swept since the last frame
                swept_angle = 0
                if circle.state != Leap.Gesture.STATE_START:
                    previous_update = CircleGesture(controller.frame(1).gesture(circle.id))
                    swept_angle =  (circle.progress - previous_update.progress) * 2 * Leap.PI

                print "  Circle id: %d, %s, progress: %f, radius: %f, angle: %f degrees, %s" % (
                        gesture.id, self.state_names[gesture.state],
                        circle.progress, circle.radius, swept_angle * Leap.RAD_TO_DEG, clockwiseness)

            if gesture.type == Leap.Gesture.TYPE_SWIPE:
                swipe = SwipeGesture(gesture)
                print "  Swipe id: %d, state: %s, position: %s, direction: %s, speed: %f" % (
                        gesture.id, self.state_names[gesture.state],
                        swipe.position, swipe.direction, swipe.speed)

            if gesture.type == Leap.Gesture.TYPE_KEY_TAP:
                keytap = KeyTapGesture(gesture)
                print "  Key Tap id: %d, %s, position: %s, direction: %s" % (
                        gesture.id, self.state_names[gesture.state],
                        keytap.position, keytap.direction )

            if gesture.type == Leap.Gesture.TYPE_SCREEN_TAP:
                screentap = ScreenTapGesture(gesture)
                print "  Screen Tap id: %d, %s, position: %s, direction: %s" % (
                        gesture.id, self.state_names[gesture.state],
                        screentap.position, screentap.direction )

        if not (frame.hands.is_empty and frame.gestures().is_empty):
            print ""

    def state_string(self, state):
        if state == Leap.Gesture.STATE_START:
            return "STATE_START"

        if state == Leap.Gesture.STATE_UPDATE:
            return "STATE_UPDATE"

        if state == Leap.Gesture.STATE_STOP:
            return "STATE_STOP"

        if state == Leap.Gesture.STATE_INVALID:
            return "STATE_INVALID"





def button_sitios():
    top = tk.Toplevel()
    top.title('Sitios de interes')
    top.wm_state('zoomed')
    
    top.update()
    screen_width = top.winfo_screenwidth()
    screen_height = top.winfo_screenheight()  
    
    global local_canvas
    
    local_canvas = tk.Canvas(top, width=screen_width, height=screen_height)
    local_canvas.place(x=screen_width/2, y=screen_height/2)
    
    init_puntero_sitios(1515, 780)


def init_puntero_sitios(x_puntero, y_puntero):
    global local_canvas    
    global btn_puntero_sitios
    
    # Puntero
    btn_puntero_sitios = tk.Button(local_canvas, background ='black')    
    btn_puntero_sitios.place(x=x_puntero, y=y_puntero, width=20, height=20)
        
    local_canvas.pack()


def actualizar_puntero_sitios(x_puntero, y_puntero):
    global btn_puntero_sitios
    
    btn_puntero_sitios.place_forget()

    init_puntero_sitios(x_puntero, y_puntero)
    
    
    


def init_interfaz():
    # Crear la ventana
    main_window = tk.Tk()
    
#    main_window.geometry(str(ANCHO_INTERFAZ) + "x" + str(ALTO_INTERFAZ+50))
    main_window.title("LeapApp")
    main_window.wm_state('zoomed')
    
    # Etiquetas
    title = tk.Label(main_window, text="¿Que desea?", font=("Helvetica",30))
    title.pack()

    main_window.update()
    screen_width = main_window.winfo_screenwidth()
    screen_height = main_window.winfo_screenheight()  
        
    
    global canvas
    canvas = tk.Canvas(main_window, width=screen_width, height=screen_height)
    canvas.place(x=screen_width/2, y=screen_height/2)
    
    crear_botones()
    
    # de 0,0 a 1500,700
    init_puntero_general(0, 0)
        
    # Ventana principal
    main_window.mainloop()



def crear_botones():
    global canvas
    
    anchura_boton = 300
    altura_boton  = 150
    
        # Fila de arriba
    btn_1 = tk.Button(canvas, text="Sitios de interes", bd='10', command=button_sitios)
    btn_2 = tk.Button(canvas, text="Ir a clase", bd='10')
    btn_3 = tk.Button(canvas, text="Horario", bd='10')
    
        # Fila de abajo
    btn_4 = tk.Button(canvas, text="Relleno", bd='10')
    btn_5 = tk.Button(canvas, text="Relleno", bd='10')
    btn_6 = tk.Button(canvas, text="Relleno", bd='10')
    
#    btn_1.grid(row=0,column=0, padx=90, pady=90)
#    btn_2.grid(row=0,column=1, padx=90, pady=90)
#    btn_3.grid(row=0,column=2, padx=90, pady=90)
#    btn_4.grid(row=1,column=0, padx=90, pady=90)
#    btn_5.grid(row=1,column=1, padx=90, pady=90)
#    btn_6.grid(row=1,column=2, padx=90, pady=90)
    
    btn_1.place(x=90, y=90, width=anchura_boton, height=altura_boton)
    btn_2.place(x=90+500, y=90, width=anchura_boton, height=altura_boton)
    btn_3.place(x=90+1000, y=90, width=anchura_boton, height=altura_boton)
    btn_4.place(x=90, y=90+300, width=anchura_boton, height=altura_boton)
    btn_5.place(x=90+500, y=90+300, width=anchura_boton, height=altura_boton)
    btn_6.place(x=90+1000, y=90+300, width=anchura_boton, height=altura_boton)
        
    canvas.pack()


def init_puntero_general(x_puntero, y_puntero):
    global canvas    
    global btn_puntero

    # Puntero
    btn_puntero = tk.Button(canvas, background ='black')    
    btn_puntero.place(x=x_puntero, y=y_puntero, width=20, height=20)
        
    canvas.pack()


def actualizar_puntero_general(x_puntero, y_puntero):
    global btn_puntero
    
    btn_puntero.place_forget()

    init_puntero_general(x_puntero, y_puntero)


def usuario_click(tiempo_cerrados):
    return




def main():
    ##############################################################################################################
    ##############################################################################################################
    ##############################################################################################################
    # Controlador de LEAP y Listener
    controller = Leap.Controller()
    listener = SampleListener()
    
    # Aniadir el listener
    controller.add_listener(listener)
    
    
    
    ##############################################################################################################
    ##############################################################################################################
    ##############################################################################################################
    # Crear la interfaz grafica
    init_interfaz()
    
    
    
    ##############################################################################################################
    ##############################################################################################################
    ##############################################################################################################
    # Finalizar el programa con la tecla ENTER
    print ("Press Enter to quit...")
    try:
        sys.stdin.readline()
    except KeyboardInterrupt:
        pass
    finally:
        # Quitar el listener
        controller.remove_listener(listener)
    


    
    
if __name__ == "__main__":
    main()