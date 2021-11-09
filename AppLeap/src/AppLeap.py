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
        normalizedPosition = interactionBox.normalize_point(hand.stabilized_palm_position)
        self.draw(normalizedPosition.x * self.ancho_canvas,
                  self.alto_canvas - normalizedPosition.y * self.alto_canvas,
                  40, 40, color='black')


    def draw(self, x, y, width, height, color):
        self.paintCanvas.create_oval( x, y, x + width, y + height, fill = color, outline = "", tags="puntero")

    def set_canvas(self, canvas):
        self.paintCanvas = canvas
        
    
    def crear_botones(self, ancho, alto):
        self.ancho_canvas = ancho
        self.alto_canvas  = alto
        
        anchura_boton = 250
        altura_boton  = 150
        
        anchura_texto = 50
        fila_offset = anchura_boton/2
        col_offset  = altura_boton/2
        
        fila1 = 50
        fila2 = fila1 + altura_boton + 50
        fila3 = fila2 + altura_boton + 50
        
        col1 = 50
        col2 = col1 + anchura_boton + 50
        col3 = col2 + anchura_boton + 50
           
        
        rect1 = self.paintCanvas.create_rectangle(col1, fila1, col1+anchura_boton, fila1+altura_boton, fill="#D3D3D3")
        btn_1 = self.paintCanvas.create_text(col1+fila_offset, fila1+col_offset, text="Sitios de interes", width=anchura_texto)
        
        rect2 = self.paintCanvas.create_rectangle(col2, fila1, col2+anchura_boton, fila1+altura_boton, fill="#D3D3D3")
        btn_2 = self.paintCanvas.create_text(col2+fila_offset, fila1+col_offset, text="Ir a clase", width=anchura_texto)
        
        rect3 = self.paintCanvas.create_rectangle(col3, fila1, col3+anchura_boton, fila1+altura_boton, fill="#D3D3D3")
        btn_3 = self.paintCanvas.create_text(col3+fila_offset, fila1+col_offset, text="Horarios", width=anchura_texto)
        
        rect4 = self.paintCanvas.create_rectangle(col1, fila2, col1+anchura_boton, fila2+altura_boton, fill="#D3D3D3")
        btn_4 = self.paintCanvas.create_text(col1+fila_offset, fila2+col_offset, text="Relleno 1", width=anchura_texto)
        
        rect5 = self.paintCanvas.create_rectangle(col2, fila2, col2+anchura_boton, fila2+altura_boton, fill="#D3D3D3")
        btn_5 = self.paintCanvas.create_text(col2+fila_offset, fila2+col_offset, text="Relleno 2", width=anchura_texto)
        
        rect6 = self.paintCanvas.create_rectangle(col3, fila2, col3+anchura_boton, fila2+altura_boton, fill="#D3D3D3")
        btn_6 = self.paintCanvas.create_text(col3+fila_offset, fila2+col_offset, text="Relleno 3", width=anchura_texto)
        
        rect7 = self.paintCanvas.create_rectangle(col2, fila3, col2+anchura_boton, fila3+altura_boton, fill="red")
        btn_7 = self.paintCanvas.create_text(col2+fila_offset, fila3+col_offset, text="Cerrar", width=anchura_texto)
        

        



class PaintBox(Frame):

    def __init__( self ):
        Frame.__init__( self )
        self.leap = Leap.Controller()
        self.painter = TouchPointListener()
        self.leap.add_listener(self.painter)
        self.pack( expand = YES, fill = BOTH )
        self.master.title( "App LEAP" )
#        self.master.geometry( "950x650" )
        self.master.wm_state('zoomed')
        
        self.ancho_canvas = 950
        self.alto_canvas  = 650
      
        # create Canvas component
        self.paintCanvas = Canvas( self, width = str(self.ancho_canvas), height = str(self.alto_canvas) )
        self.paintCanvas.pack()
        self.painter.set_canvas(self.paintCanvas)
        self.painter.crear_botones(self.ancho_canvas, self.alto_canvas)



def main():
    PaintBox().mainloop()

if __name__ == "__main__":
    main()