/**
 * Un peu de java doc
 */
package com.siat.protocole.karine;

public enum EtatCamera {
	OK(0,"0k"),
ABSENCE_SIGNAL (1,"Absence signal"),
CAMERA_ENCRASSEE(2,"Caméra encrassée"),
CAMERA_DEPLACEE(4,"Caméra déplacée"),
ERREUR_INTERNE(8,"Erreur interne analyseur");
	private int valeur;
	private String info="";
	private EtatCamera(int val,String inf)
	{
		valeur=val;
		info=inf;
	}
	public int getValue()
	{
		return valeur;
	}
	public boolean isAffected(int val)
	{
		return (valeur&val)!=0;
	}
	public String toString()
	{
		return info;
	}

}
