# -*- coding: utf-8 -*-
"""
Created on Mon Nov  8 19:02:36 2021

@author: mario
"""

import Tkinter as tk
from tkinter import * 
from Tkinter import Frame, Canvas, YES, BOTH
from PIL import ImageTk
import sys
import time

sys.path.insert(0, "../lib")
import Leap

class TouchPointListener(Leap.Listener):
    def __init__(self, ventana_main, ancho, alto):
        super(TouchPointListener, self).__init__()  #Initialize like a normal listener
        
        self.paintBox = ventana_main
        self.localCanvas = None
        
        self.ancho_canvas = ancho
        self.alto_canvas  = alto
        
        self.dedos_extendidos = False
        self.dedos_cerrados_tras_extendidos = False
        self.tiempo_ultimo_extendidos = 0
        self.tiempo_posicion_neutra = 0

        self.pos_x_user = 0
        self.pos_y_user = 0
        
        self.anchura_boton = 250
        self.altura_boton  = 150
        
        self.fila1 = 50
        self.fila2 = self.fila1 + self.altura_boton + 50
        self.fila3 = self.fila2 + self.altura_boton + 50
        self.fila4 = self.fila3 + self.altura_boton + 50
        
        
        self.col1 = 50
        self.col2 = self.col1 + self.anchura_boton + 50
        self.col3 = self.col2 + self.anchura_boton + 50
        
        self.anchura_texto = 50
        self.fila_offset = self.anchura_boton/2
        self.col_offset  = self.altura_boton/2
        
        self.puntero = None
        self.punteroTop = None
        
        # Lista de imagenes y descripciones de sitios
        self.photos = [ImageTk.PhotoImage(file='../imagenes/imagen1.png'), 
                  ImageTk.PhotoImage(file='../imagenes/imagen2.jpg'),  
                  ImageTk.PhotoImage(file='../imagenes/imagen3.jpeg')]
        
        self.description = ["Descripcion sitio 1", "Descripcion sitio 2", "Descripcion sitio 3"]
        # Puntero que indica en que sitio estamos
        self.puntero_sitios = 0
        self.inSitios = False
        
        
    def on_init(self, controller):
        print "Initialized"

    def on_connect(self, controller):
        print "Connected"

    def on_frame(self, controller):
        if not self.paintCanvas is None:
            if self.puntero in self.paintCanvas.find_all():
                self.paintCanvas.delete("puntero")
                self.puntero = None
        
        if not self.localCanvas is None:
            if self.punteroTop in self.localCanvas.find_all():
                self.localCanvas.delete("punteroTop")
                self.punteroTop = None
        
        frame = controller.frame()

        interactionBox = frame.interaction_box
            
        handlist = frame.hands
        hand = handlist[0]
        position_hand = hand.stabilized_palm_position
        normalizedPosition = interactionBox.normalize_point(position_hand)
        
        orientation_palm = hand.palm_normal

        self.pos_x_user = normalizedPosition.x * self.ancho_canvas
        self.pos_y_user = self.alto_canvas - normalizedPosition.y * self.alto_canvas
        
        self.draw(self.pos_x_user, self.pos_y_user, 40, 40, color='black')
        


        fingerList = frame.fingers
        if len(fingerList) > 0:
            extendedFingers = len(fingerList.extended())
            
            
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
                tiempo_cerrados = tiempo_volver_abrir - self.tiempo_ultimo_extendidos
                
                if 1 < tiempo_cerrados and tiempo_cerrados < 3:
                    self.usuario_click()

            if (orientation_palm[0] < -0.9 and extendedFingers == 5):
                cont = 0
                for i in hand.fingers:
                    if (i.direction[0]< 0.1 and i.direction[0] > -0.3):
                        cont+=1
                if (cont >= 3):
                    self.tiempo_posicion_neutra = time.time()
            elif (((time.time() - self.tiempo_posicion_neutra) < 400)):
                cont = 0
                for i in hand.fingers:
                    if (i.direction[0] < -0.3):
                        cont+=1
                    elif(i.direction[0] > 0.2):
                        cont-=1
                if (cont >= 4 and self.inSitios):
                    
                    self.puntero_sitios = (self.puntero_sitios + 1 ) % len(self.photos)
                    self.tiempo_posicion_neutra -= 400
                    
                elif(cont <= -4 and self.inSitio):
                    self.puntero_sitios = (self.puntero_sitios - 1 ) % len(self.photos)                    
                    self.tiempo_posicion_neutra -= 400

                            
                

                


    def draw(self, x, y, width, height, color):
        if not self.paintCanvas is None:
            self.puntero = self.paintCanvas.create_oval( x, y, x + width, y + height, fill = color, outline = "", tags="puntero")
        
        if not self.localCanvas is None:
            self.punteroTop = self.localCanvas.create_oval( x, y, x + width, y + height, fill = color, outline = "", tags="punteroTop")
            

    def set_canvas(self, canvasMain, canvasSitios):
        self.paintCanvas = canvasMain
        self.localCanvas = canvasSitios
        
    
    def crear_botones_paintCanvas(self):
        # Main Window
        rect_sitios = self.paintCanvas.create_rectangle(self.col1, self.fila1, self.col1+self.anchura_boton,
                                                        self.fila1+self.altura_boton, fill="#D3D3D3")
                                                        
        btn_sitios = self.paintCanvas.create_text(self.col1+self.fila_offset, self.fila1+self.col_offset,
                                                  text="Sitios de interes", width=self.anchura_texto)
        
        #############
        rect2 = self.paintCanvas.create_rectangle(self.col2, self.fila1, self.col2+self.anchura_boton,
                                                  self.fila1+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_2 = self.paintCanvas.create_text(self.col2+self.fila_offset, self.fila1+self.col_offset,
                                             text="Ir a clase", width=self.anchura_texto)
        
        #############
        rect3 = self.paintCanvas.create_rectangle(self.col3, self.fila1, self.col3+self.anchura_boton,
                                                  self.fila1+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_3 = self.paintCanvas.create_text(self.col3+self.fila_offset, self.fila1+self.col_offset,
                                             text="Horarios", width=self.anchura_texto)
        
        #############
        rect4 = self.paintCanvas.create_rectangle(self.col1, self.fila2, self.col1+self.anchura_boton,
                                                  self.fila2+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_4 = self.paintCanvas.create_text(self.col1+self.fila_offset, self.fila2+self.col_offset,
                                             text="Relleno 1", width=self.anchura_texto)
        
        #############
        rect5 = self.paintCanvas.create_rectangle(self.col2, self.fila2, self.col2+self.anchura_boton,
                                                  self.fila2+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_5 = self.paintCanvas.create_text(self.col2+self.fila_offset, self.fila2+self.col_offset,
                                             text="Relleno 2", width=self.anchura_texto)
        
        #############
        rect6 = self.paintCanvas.create_rectangle(self.col3, self.fila2, self.col3+self.anchura_boton,
                                                  self.fila2+self.altura_boton, fill="#D3D3D3")
                                                  
        btn_6 = self.paintCanvas.create_text(self.col3+self.fila_offset, self.fila2+self.col_offset,
                                             text="Relleno 3", width=self.anchura_texto)
        
        #############
        rect_cerrarMain = self.paintCanvas.create_rectangle(self.col2, self.fila3, self.col2+self.anchura_boton,
                                                            self.fila3+self.altura_boton, fill="red")
        
        btn_cerrarMain = self.paintCanvas.create_text(self.col2+self.fila_offset, self.fila3+self.col_offset,
                                                      text="Cerrar", width=self.anchura_texto)
        
    
    def crear_botones_localCanvas(self):
        # Sitios window
        rect_imagen_sitio = self.localCanvas.create_image(650, 400, image=self.photos[self.puntero_sitios], anchor = 's')
        
        rec_desc_rect_sitio =  self.localCanvas.create_rectangle(350, 400, 700,
                                                              450, fill="blue")
        
        text_descripcion_sitios = self.localCanvas.create_text(400, 425,
                                                        text=self.description[self.puntero_sitios], width=150)
        
        
        rect_cerrarSitios = self.localCanvas.create_rectangle(self.col2, self.fila3 , self.col2+self.anchura_boton,
                                                              self.fila3+self.altura_boton, fill="red")
        
        btn_cerrarSitios = self.localCanvas.create_text(self.col2+self.fila_offset, self.fila3+self.col_offset,
                                                        text="Cerrar", width=self.anchura_texto)
        
        
    def usuario_click(self):
        """
        Main Window:
            Boton de ir a sitios:   self.col1,self.fila1 a
                                    self.col1+self.anchura_boton,self.fila1+self.altura_boton
            Boton cerrar:   self.col2,self.fila3 a
                            self.col2+self.anchura_boton,self.fila3+self.altura_boton
        Top Level:
            Boton cerrar: 665,640 a 795,695
        """
        
        if self.localCanvas is None:
            # Boton sitios
            if (self.col1 <= self.pos_x_user and self.pos_x_user <= self.col1+self.anchura_boton and
                self.fila1 <= self.pos_y_user and self.pos_y_user <= self.fila1+self.altura_boton):
                self.inSitios = True
                self.button_sitios_accion()
        
            # Boton cerrar de la general
            if (self.col2 <= self.pos_x_user and self.pos_x_user <= self.col2+self.anchura_boton and
                self.fila3 <= self.pos_y_user and self.pos_y_user <= self.fila3+self.altura_boton):
                self.cerrar_window_general()
        else:
            # Boton cerrar de la general
            if (self.col2 <= self.pos_x_user and self.pos_x_user <= self.col2+self.anchura_boton and
                self.fila3 <= self.pos_y_user and self.pos_y_user <= self.fila3+self.altura_boton):
                self.inSitios = False
                self.cerrar_window_sitios()
        
        
    def button_sitios_accion(self):
        if not self.paintCanvas is None:
            self.paintCanvas.destroy()
            self.paintCanvas = None

        self.createLocalCanvas()
        

    def cerrar_window_general(self):
        self.paintBox.quitarListener()
        
        canvasVacio = Canvas( self.paintBox, width = str(self.ancho_canvas), height = str(self.alto_canvas) )
        canvasVacio.pack()
        
        if not self.paintCanvas is None:
            self.paintCanvas.destroy()
            self.paintCanvas = None
        
        if not self.localCanvas is None:
            self.localCanvas.destroy()
            self.localCanvas = None
            
    
    def cerrar_window_sitios(self):
        self.createPaintCanvas()
        
        if not self.localCanvas is None:
            self.localCanvas.destroy()
            self.localCanvas = None
        
        
    def createPaintCanvas(self):
        # create main Canvas component
        self.paintCanvas = Canvas( self.paintBox, width = str(self.ancho_canvas), height = str(self.alto_canvas) )
        self.paintCanvas.pack()
        self.crear_botones_paintCanvas()
        
    
    def createLocalCanvas(self):
        # create local Canvas component for sitios
        self.localCanvas = Canvas( self.paintBox, width = str(self.ancho_canvas), height = str(self.alto_canvas) )
        self.localCanvas.pack()
        self.crear_botones_localCanvas()
    


class PaintBox(Frame):

    def __init__( self ):
        Frame.__init__( self )
        
        self.ancho_canvas = 950
        self.alto_canvas  = 650
        
        self.leap = Leap.Controller()
        self.painter = TouchPointListener(self, self.ancho_canvas, self.alto_canvas)
        self.leap.add_listener(self.painter)
        self.pack( expand = YES, fill = BOTH )
        self.master.title( "App LEAP" )
#        self.master.geometry( "950x650" )
        self.master.wm_state('zoomed')
        
        self.painter.createPaintCanvas()
      
#        # create main Canvas component
#        self.paintCanvas = Canvas( self, width = str(self.ancho_canvas), height = str(self.alto_canvas) )
#        self.paintCanvas.pack()
        
#        # create local Canvas component for sitios
#        self.paintCanvasSitios = Canvas( self, width = str(self.ancho_canvas), height = str(self.alto_canvas) )
#        
#        self.painter.set_canvas(self.paintCanvas, self.paintCanvasSitios)
        
#        self.painter.crear_botones()
    
    def quitarListener(self):
        self.leap.remove_listener(self.painter)


def main():
    PaintBox().mainloop()

if __name__ == "__main__":
    main()