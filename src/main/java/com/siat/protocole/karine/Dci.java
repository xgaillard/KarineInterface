/**
 * Ajout d'un entete javadoc
 */
package com.siat.protocole.karine;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class Dci {
	byte anzId;

	byte voie;

	GregorianCalendar cal;

	int sensc;

	int vitesse;

	int longueur;

	int tempsPresence;

	int pl;

	int silhouette;

	int confiance;

	public Dci()
	{
		this((byte)0,(byte)0,new GregorianCalendar(),0,0,0,0,0,0,0);
	}
	public Dci(int anzId_uc, int voie_uc, GregorianCalendar calendrier,
			int sens_uc, int vitesse_uc, int longueur_uc, int tempsPresence_uw,
			int pl_uc, int silh_uc, int confiance_uc) {
		anzId = (byte)anzId_uc;
		voie = (byte)voie_uc;
		cal=calendrier;
		sensc = sens_uc;

		longueur = longueur_uc;
		setVitesse(vitesse_uc);
		//tempsPresence = tempsPresence_uw;
		pl = pl_uc;
		silhouette = silh_uc;
		confiance = confiance_uc;
	};
	public String toString()
	{
	SimpleDateFormat df=new SimpleDateFormat("HH:mm:ss.SSS");
	StringBuffer sb=new StringBuffer();
		sb.append(""+voie+": ");
		sb.append(df.format(cal.getTime()));
		sb.append(" V="+vitesse);
		sb.append(" TP="+tempsPresence);
		return sb.toString();
	}
	public long getTimeInMillis()
	{
		return cal.getTimeInMillis();
	}
	public GregorianCalendar getCal() {
		// TODO Auto-generated method stub
		return cal;
	}
	public int getVoie() {
		// TODO Auto-generated method stub
		return (int)voie;
	}
	public void setVitesse(int vout) {
		vitesse=vout;
		if(vout<=0)
		{
			vout=1;
		}
		tempsPresence=(int)((longueur*360.)/vitesse);
	}
	public double getVitesse() {
		// TODO Auto-generated method stub
		return (double)vitesse;
	}
}

