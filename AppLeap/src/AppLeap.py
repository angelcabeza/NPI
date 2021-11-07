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


RADIO_PUNTERO = 20


path_puntero = "../imagenes/puntero.png"




class SampleListenerConInterfaz(Leap.Listener):
    finger_names = ['Thumb', 'Index', 'Middle', 'Ring', 'Pinky']
    bone_names = ['Metacarpal', 'Proximal', 'Intermediate', 'Distal']
    state_names = ['STATE_INVALID', 'STATE_START', 'STATE_UPDATE', 'STATE_END']
    
    
    def __init__(self):
        super(SampleListenerConInterfaz, self).__init__()  #Initialize like a normal listener
        
        self.dedos_extendidos = False
        self.dedos_cerrados_tras_extendidos = False
        self.tiempo_ultimo_extendidos = 0
        self.canvas = None
        self.local_canvas = None
        self.btn_puntero_sitios = None
        self.btn_puntero = None
        self.main_window = None
        self.top_window = None
        self.pos_x_user = 0
        self.pos_y_user = 0
        self.ancho_ventana = 0
        self.alto_ventana = 0
        
        self.init_interfaz()


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
            
            self.pos_x_user = normalizedPoint.x * self.ancho_ventana
            self.pos_y_user = (1 - normalizedPoint.y) * self.alto_ventana
            
            self.actualizar_punteros()
            self.actualizar_punteros()
    
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
                self.dedos_extendidos = False
                self.dedos_cerrados_tras_extendidos = True
                
            elif extendedFingers == 5 and self.dedos_cerrados_tras_extendidos:
                self.dedos_extendidos = True
                self.dedos_cerrados_tras_extendidos = False
                
                tiempo_volver_abrir = time.time()
                tiempo_cerrados = self.tiempo_ultimo_extendidos - tiempo_volver_abrir
                self.usuario_click(tiempo_cerrados)
                
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


    
    def init_interfaz(self):
        # Crear la ventana
        self.main_window = tk.Tk()
        
#        main_window.geometry(str(ANCHO_INTERFAZ) + "x" + str(ALTO_INTERFAZ+50))
        self.main_window.title("LeapApp")
        self.main_window.wm_state('zoomed')
        
        # Etiquetas
        title = tk.Label(self.main_window, text="¿Que desea?", font=("Helvetica",30))
        title.pack()
    
        self.get_dimensiones_actuales()
            
        self.canvas = tk.Canvas(self.main_window, width=self.ancho_ventana, height=self.alto_ventana-100)
        self.canvas.place(x=0, y=50)
        
        self.crear_botones()

        
        # de 0,0 a 1500,700
        self.init_puntero_general(self.ancho_ventana-20, self.alto_ventana-134)
            
        # Ventana principal
        self.main_window.mainloop()
    
    
    def get_dimensiones_actuales(self):
        self.main_window.update()
        self.ancho_ventana = self.main_window.winfo_screenwidth()
        self.alto_ventana = self.main_window.winfo_screenheight()
        
        if self.top_window is not None:
            self.ancho_ventana = self.top_window.winfo_screenwidth()
            self.alto_ventana = self.top_window.winfo_screenheight() - 64
    
    
    def crear_botones(self):        
        anchura_boton = 300
        altura_boton  = 150
        
            # Fila de arriba
        btn_1 = tk.Button(self.canvas, text="Sitios de interes", bd='20', command=self.button_sitios_accion)
        btn_2 = tk.Button(self.canvas, text="Ir a clase", bd='20')
        btn_3 = tk.Button(self.canvas, text="Horario", bd='20')
        
            # Fila de abajo
        btn_4 = tk.Button(self.canvas, text="Relleno", bd='20')
        btn_5 = tk.Button(self.canvas, text="Relleno", bd='20')
        btn_6 = tk.Button(self.canvas, text="Relleno", bd='20')
        
        btn_1.place(x=90, y=90, width=anchura_boton, height=altura_boton)
        btn_2.place(x=90+500, y=90, width=anchura_boton, height=altura_boton)
        btn_3.place(x=90+1000, y=90, width=anchura_boton, height=altura_boton)
        btn_4.place(x=90, y=90+250, width=anchura_boton, height=altura_boton)
        btn_5.place(x=90+500, y=90+250, width=anchura_boton, height=altura_boton)
        btn_6.place(x=90+1000, y=90+250, width=anchura_boton, height=altura_boton)
        
        
        btn_cerrar = tk.Button(self.canvas, text="Cerrar", bd='20', background='red',
                               command=self.cerrar_window_general)
        btn_cerrar.place(x=90+575, y=90+500, width=150, height=75)
                
    
    def init_puntero_general(self, x_puntero, y_puntero):
        # Puntero
        self.btn_puntero = tk.Button(self.canvas, background ='black')    
        self.btn_puntero.place(x=x_puntero, y=y_puntero, width=20, height=20)
                
    
    def actualizar_puntero_general(self):        
        self.btn_puntero.place_forget()
    
        self.init_puntero_general(self.pos_x_user, self.pos_y_user)
    
    
    
    def button_sitios_accion(self):
        self.top_window = tk.Toplevel()
        self.top_window.title('Sitios de interes')
        self.top_window.wm_state('zoomed')
        self.top_window.protocol("WM_DELETE_WINDOW", self.cerrar_window_sitios)
        
        self.get_dimensiones_actuales()  
                
        self.local_canvas = tk.Canvas(self.top_window, width=self.ancho_ventana, height=self.alto_ventana)
        self.local_canvas.place(x=0, y=0)
        
        self.init_puntero_sitios(self.ancho_ventana-20, self.alto_ventana-20)
        
        btn_cerrar = tk.Button(self.local_canvas, text="Cerrar", bd='20', background='red',
                               command=self.cerrar_window_sitios)
        btn_cerrar.place(x=0, y=0, width=150, height=75)
    
    
    def init_puntero_sitios(self, x_puntero=None, y_puntero=None):
        if x_puntero is None:
            x_puntero = self.x_puntero
        
        if y_puntero is None:
            y_puntero = self.y_puntero
        
        # Puntero
        self.btn_puntero_sitios = tk.Button(self.local_canvas, background ='black')    
        self.btn_puntero_sitios.place(x=x_puntero, y=y_puntero, width=20, height=20)
                
    
    def actualizar_puntero_sitios(self):        
        self.btn_puntero_sitios.place_forget()
    
        self.init_puntero_sitios(self.pos_x_user, self.pos_y_user)
    
    
    def usuario_click(self, tiempo_cerrados):
        if self.top_window is None:
            self.pos_x_user, self.pos_y_user


    def actualizar_punteros(self):
        if self.top_window is None:
            self.actualizar_puntero_general()
        else:
            self.actualizar_puntero_general()
            
            
    def cerrar_window_general(self):
        self.main_window.destroy()
    
    
    def cerrar_window_sitios(self):
        self.top_window.destroy()
        self.top_window = None
        
        

def main():
    ##############################################################################################################
    ##############################################################################################################
    ##############################################################################################################
    # Controlador de LEAP y Listener
    controller = Leap.Controller()
    listener = SampleListenerConInterfaz()
    
    # Aniadir el listener
    controller.add_listener(listener)
    
    
    
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