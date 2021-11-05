#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Nov  4 10:39:06 2021

@author: angel perro
"""


import tkinter as tk
import os, sys

# Insertar el path a las librerias
sys.path.insert(0, "../lib")

import Leap
from Leap import CircleGesture, KeyTapGesture, ScreenTapGesture, SwipeGesture





def button_sitios():
    top = tk.Toplevel()
    top.title('Sitios de interes')
    top.wm_state('zoomed')





def main():
    # Crear ka ventana
    main_window = tk.Tk()
    main_window.geometry("500x500")
    main_window.title("LeapApp")
    
    # Etiquetas
    tk.Label(main_window, text="¿Que desea?",justify="left",font=("Helvetica",30)).grid(row=0, column = 3)

    # Botones
        #Fila de arriba
    tk.Button(main_window, text="Sitios de interes", padx=95, pady=50, command=button_sitios).grid(row=1, column = 2, padx=35)
    tk.Button(main_window, text="Ir a clase", padx=95, pady=50).grid(row=1,column=3, padx = 200 ,pady=100)
    tk.Button(main_window, text="Horario", padx=95, pady=50).grid(row=1,column=4)
    
        # Fila de abajo
    tk.Button(main_window, text="Relleno", padx=95, pady=50).grid(row=2,column=2)
    tk.Button(main_window, text="Relleno", padx=95, pady=50).grid(row=2,column=3)
    tk.Button(main_window, text="Relleno", padx=95, pady=50).grid(row=2,column=4)
    
    # Ventana principal
    main_window.wm_state('zoomed')
    main_window.mainloop()
    


    
    
if __name__ == "__main__":
    main()