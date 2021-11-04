#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Nov  4 10:39:06 2021

@author: angel
"""


import tkinter as tk

main_window = tk.Tk()
main_window.geometry("500x500")
main_window.title("LeapApp")

#Labels
texto = tk.Label(main_window, text="¿Qué desea?",justify="left",font=("Helvetica",45)).grid(row=0, column = 3)

def button_sitios():
    top = tk.Toplevel()
    top.title('Sitios de interes')
    top.attributes('-zoomed', True)

# Buttons

#Fila de arriba
tk.Button(main_window, text="Sitios de interes", padx=200, pady=100, command=button_sitios).grid(row=1, column = 2, padx=35)
tk.Button(main_window, text="Ir a clase", padx=200, pady=100).grid(row=1,column=3, padx = 200 ,pady=100)
tk.Button(main_window, text="Horario", padx=200, pady=100).grid(row=1,column=4)

# Fila de abajo
tk.Button(main_window, text="Relleno", padx=200, pady=100).grid(row=2,column=2)
tk.Button(main_window, text="Relleno", padx=200, pady=100).grid(row=2,column=3)
tk.Button(main_window, text="Relleno", padx=200, pady=100).grid(row=2,column=4)

main_window.attributes('-zoomed', True)
main_window.mainloop()