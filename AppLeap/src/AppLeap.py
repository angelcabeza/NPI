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


ANCHO_INTERFAZ = 500
ALTO_INTERFAZ = 500
RADIO_PUNTERO = 20




class SampleListener(Leap.Listener):
    finger_names = ['Thumb', 'Index', 'Middle', 'Ring', 'Pinky']
    bone_names = ['Metacarpal', 'Proximal', 'Intermediate', 'Distal']
    state_names = ['STATE_INVALID', 'STATE_START', 'STATE_UPDATE', 'STATE_END']

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
            
            actualizar_puntero(app_x, app_y)
    
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

            # Get fingers
            for finger in hand.fingers:

                print "    %s finger, id: %d, length: %fmm, width: %fmm" % (
                    self.finger_names[finger.type],
                    finger.id,
                    finger.length,
                    finger.width)

                # Get bones
                for b in range(0, 4):
                    bone = finger.bone(b)
                    print "      Bone: %s, start: %s, end: %s, direction: %s" % (
                        self.bone_names[bone.type],
                        bone.prev_joint,
                        bone.next_joint,
                        bone.direction)

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


def actualizar_puntero(new_x, new_y):
    x, y = new_x + 3, new_y + 7  
    #the addition is just to center the oval around the center of the mouse
    #remove the the +3 and +7 if you want to center it around the point of the mouse
    global circle
    global canvas
    
    canvas.delete(circle)  #to refresh the circle each motion

    x_max = x + RADIO_PUNTERO
    x_min = x - RADIO_PUNTERO
    y_max = y + RADIO_PUNTERO
    y_min = y - RADIO_PUNTERO

    circle = canvas.create_oval(x_max, y_max, x_min, y_min, outline="black")
    
    


def init_interfaz():
    # Crear la ventana
    main_window = tk.Tk()
    
    main_window.geometry(str(ANCHO_INTERFAZ) + "x" + str(ALTO_INTERFAZ))
    main_window.title("LeapApp")
    
    # Etiquetas
    tk.Label(main_window, text="¿Que desea?",justify="left",font=("Helvetica",30)).grid(row=0, column = 3)

    # Botones
    anchura_boton = 95
    altura_boton  = 50
        #Fila de arriba
    tk.Button(main_window, text="Sitios de interes", padx=anchura_boton, pady=altura_boton,
              command=button_sitios).grid(row=1, column = 2, padx=35)
    
    tk.Button(main_window, text="Ir a clase", padx=anchura_boton,
              pady=altura_boton).grid(row=1,column=3, padx = 200 ,pady=100)
    
    tk.Button(main_window, text="Horario", padx=anchura_boton, pady=altura_boton).grid(row=1,column=4)
    
        # Fila de abajo
    tk.Button(main_window, text="Relleno", padx=anchura_boton, pady=altura_boton).grid(row=2,column=2)
    tk.Button(main_window, text="Relleno", padx=anchura_boton, pady=altura_boton).grid(row=2,column=3)
    tk.Button(main_window, text="Relleno", padx=anchura_boton, pady=altura_boton).grid(row=2,column=4)
    
        # Puntero
    global circle
    circle = 0
        
    global canvas
    canvas = tk.Canvas(main_window)
    canvas.pack()
    
    prueba = 10
    actualizar_puntero(prueba, prueba)
    
    # Ventana principal
    #main_window.wm_state('zoomed')
    main_window.mainloop()
    



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