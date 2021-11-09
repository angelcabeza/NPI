# -*- coding: utf-8 -*-
"""
Created on Mon Nov  8 19:02:36 2021

@author: mario
"""

import Tkinter as tk
from Tkinter import Frame, Canvas, YES, BOTH
import sys
sys.path.insert(0, "../lib")
import Leap

class TouchPointListener(Leap.Listener):
    def __init__(self, ancho, alto):
        super(TouchPointListener, self).__init__()  #Initialize like a normal listener
        self.ancho_canvas = ancho
        self.alto_canvas  = alto
        
#        self.dedos_extendidos = False
#        self.dedos_cerrados_tras_extendidos = False
#        self.tiempo_ultimo_extendidos = 0
#        self.canvas = None
#        self.local_canvas = None
#        self.btn_puntero_sitios = None
#        self.btn_puntero = None
#        self.main_window = None
#        self.top_window = None
        self.pos_x_user = 0
        self.pos_y_user = 0
#        self.ancho_ventana = 0
#        self.alto_ventana = 0
        
        self.anchura_boton = 250
        self.altura_boton  = 150
        
        self.fila1 = 50
        self.fila2 = self.fila1 + self.altura_boton + 50
        self.fila3 = self.fila2 + self.altura_boton + 50
        
        self.col1 = 50
        self.col2 = self.col1 + self.anchura_boton + 50
        self.col3 = self.col2 + self.anchura_boton + 50
        
        
    def on_init(self, controller):
        print "Initialized"

    def on_connect(self, controller):
        print "Connected"

    def on_frame(self, controller):
        self.paintCanvas.delete("puntero")
        frame = controller.frame()

        interactionBox = frame.interaction_box
            
        handlist = frame.hands
        hand = handlist[0]
        position_hand = hand.stabilized_palm_position
        normalizedPosition = interactionBox.normalize_point(position_hand)
        
        self.pos_x_user = normalizedPosition.x * self.ancho_canvas
        self.pos_y_user = self.alto_canvas - normalizedPosition.y * self.alto_canvas
        
        self.draw(self.pos_x_user, self.pos_y_user, 40, 40, color='black')
        
        fingerList = frame.fingers
        if len(fingerList) > 0:
            extendedFingers = len(fingerList.extended())
            
            if extendedFingers == 0:
                self.usuario_click(2)
        
#        if extendedFingers == 5 and not self.dedos_cerrados_tras_extendidos:
#            self.tiempo_ultimo_extendidos = time.time()
#            self.dedos_extendidos = True
#            
#        elif extendedFingers == 0 and self.dedos_extendidos:
#            self.dedos_extendidos = False
#            self.dedos_cerrados_tras_extendidos = True
#            
#        elif extendedFingers == 5 and self.dedos_cerrados_tras_extendidos:
#            self.dedos_extendidos = True
#            self.dedos_cerrados_tras_extendidos = False
#            
#            tiempo_volver_abrir = time.time()
#            tiempo_cerrados = self.tiempo_ultimo_extendidos - tiempo_volver_abrir
#            #self.usuario_click(tiempo_cerrados)
#            print "pillado"
#            
#        else:
#            self.dedos_extendidos = False
#            self.dedos_cerrados_tras_extendidos = False


    def draw(self, x, y, width, height, color):
        self.paintCanvas.create_oval( x, y, x + width, y + height, fill = color, outline = "", tags="puntero")

    def set_canvas(self, canvas):
        self.paintCanvas = canvas
        
    
    def crear_botones(self):
        anchura_texto = 50
        fila_offset = self.anchura_boton/2
        col_offset  = self.altura_boton/2
           
        #############
        rect_sitios = self.paintCanvas.create_rectangle(self.col1, self.fila1, self.col1+self.anchura_boton,
                                                        self.fila1+self.altura_boton, fill="#D3D3D3")
                                                        
        btn_sitios = self.paintCanvas.create_text(self.col1+fila_offset, self.fila1+col_offset,
                                                  text="Sitios de interes", width=anchura_texto)
        
        #############
        rect2 = self.paintCanvas.create_rectangle(self.col2, self.fila1, self.col2+self.anchura_boton,
                                                  self.fila1+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_2 = self.paintCanvas.create_text(self.col2+fila_offset, self.fila1+col_offset,
                                             text="Ir a clase", width=anchura_texto)
        
        #############
        rect3 = self.paintCanvas.create_rectangle(self.col3, self.fila1, self.col3+self.anchura_boton,
                                                  self.fila1+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_3 = self.paintCanvas.create_text(self.col3+fila_offset, self.fila1+col_offset,
                                             text="Horarios", width=anchura_texto)
        
        #############
        rect4 = self.paintCanvas.create_rectangle(self.col1, self.fila2, self.col1+self.anchura_boton,
                                                  self.fila2+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_4 = self.paintCanvas.create_text(self.col1+fila_offset, self.fila2+col_offset,
                                             text="Relleno 1", width=anchura_texto)
        
        #############
        rect5 = self.paintCanvas.create_rectangle(self.col2, self.fila2, self.col2+self.anchura_boton,
                                                  self.fila2+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_5 = self.paintCanvas.create_text(self.col2+fila_offset, self.fila2+col_offset,
                                             text="Relleno 2", width=anchura_texto)
        
        #############
        rect6 = self.paintCanvas.create_rectangle(self.col3, self.fila2, self.col3+self.anchura_boton,
                                                  self.fila2+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_6 = self.paintCanvas.create_text(self.col3+fila_offset, self.fila2+col_offset,
                                             text="Relleno 3", width=anchura_texto)
        
        #############
        rect_cerrar = self.paintCanvas.create_rectangle(self.col2, self.fila3, self.col2+self.anchura_boton,
                                                        self.fila3+self.altura_boton, fill="red")
        
        btn_cerrar = self.paintCanvas.create_text(self.col2+fila_offset, self.fila3+col_offset,
                                                  text="Cerrar", width=anchura_texto)
        

    def usuario_click(self, tiempo_cerrados):
        """
        Main Window:
            Boton de ir a sitios:   self.col1,self.fila1 a
                                    self.col1+self.anchura_boton,self.fila1+self.altura_boton
            Boton cerrar:   self.col2,self.fila3 a
                            self.col2+self.anchura_boton,self.fila3+self.altura_boton
        Top Level:
            Boton cerrar: 665,640 a 795,695
        """
#        if tiempo_cerrados >= 1 and tiempo_cerrados <= 2: # Estar un segundo con las manos cerradas
#            if self.top_window is None:
#                if (self.pos_x_user >= 90 and self.pos_x_user <= 370 and
#                    self.pos_y_user >= 90 and self.pos_y_user <= 220):
#                    self.button_sitios_accion()
#                
#                elif (self.pos_x_user >= 665 and self.pos_x_user <= 795 and
#                      self.pos_y_user >= 590 and self.pos_y_user <= 645):
#                    self.cerrar_window_general()
#            else:
#                if (self.pos_x_user >= 665 and self.pos_x_user <= 795 and
#                    self.pos_y_user >= 640 and self.pos_y_user <= 695):
#                    self.cerrar_window_sitios()
        
        # Boton cerrar de la general
        print self.pos_x_user
        print self.pos_y_user
        if (self.pos_x_user >= self.col2 and self.pos_x_user <= self.col2+self.anchura_boton and
            self.pos_y_user >= self.fila3 and self.pos_y_user <= self.fila3+self.altura_boton):
            self.cerrar_window_general()
            print "conseguido"


    def cerrar_window_general(self):
        self.main_window.destroy()
    
    
    def cerrar_window_sitios(self):
        self.top_window.destroy()
        self.top_window = None


class PaintBox(Frame):

    def __init__( self ):
        Frame.__init__( self )
        
        self.ancho_canvas = 950
        self.alto_canvas  = 650
        
        self.leap = Leap.Controller()
        self.painter = TouchPointListener(self.ancho_canvas, self.alto_canvas)
        self.leap.add_listener(self.painter)
        self.pack( expand = YES, fill = BOTH )
        self.master.title( "App LEAP" )
#        self.master.geometry( "950x650" )
        self.master.wm_state('zoomed')
      
        # create Canvas component
        self.paintCanvas = Canvas( self, width = str(self.ancho_canvas), height = str(self.alto_canvas) )
        self.paintCanvas.pack()
        self.painter.set_canvas(self.paintCanvas)
        self.painter.crear_botones()



def main():
    PaintBox().mainloop()

if __name__ == "__main__":
    main()