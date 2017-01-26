package com.siat.protocole.karine;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Vector;

public class KsccTrame {
	static int TRAME_MAXSIZE=1024;
	static byte STX=2;
	static int TRAME_ISIZE0=1;
	static int TRAME_ISIZE1=2;
	byte mainId; // identifiant systeme
	byte anzId_uc; // identifiant analyseur
	Type cmdId; // identifiant commande
	byte buffer_auc[]=null;
	int size_uw;
	boolean ready_b;

	private enum Retour {
		ERROR      (0),
		OK         (1),
		INCOMPLETE (2);
		final int valeur;
		private Retour(int val)
		{
			valeur=val;
		}
		public int value()
		{
			return valeur;
		}

	}
	static public enum Type {
		CMD_CONFIG_WRITE    (0,"CMD_CONFIG_WRITE"),
		CMD_CONFIG_READ     (1,"CMD_CONFIG_READ"),
		CMD_VERSION_READ    (2,"CMD_VERSION_READ"),
		CMD_IMAGE_READ      (3,"CMD_IMAGE_READ"),
		CMD_MEASURE_READ    (4,"CMD_MESURE_READ"),
		CMD_TIMESTAMP_WRITE (5,"CMD_TIMESTAMP_WRITE"),
		CMD_TIMESTAMP_READ  (6,"CMD_TIMESTAMP_READ"),
		CMD_INCONNUE  		(99,"CMD_INCONNUE");
		private int value;
		private String info;
		private	 Type(int valeur,String info_p)
		{
			value=valeur;
			info=info_p;
		}
		public int value()
		{
			return value;
		}
		public String toString()
		{
			return info;
		}
		static public Type getEnum(int valeur)
		{
			Type val=CMD_INCONNUE;
			for (Type info : Type.values()) {
				if(info.value()==valeur)
				{
					val=info;
					break;
				}

			}
			return val;
		}
	}
	public KsccTrame()
	{
		buffer_auc=new byte[TRAME_MAXSIZE];
	}
	public Integer decapsulateBis(
			byte buffer_puc[], 
			int size_uw,
			byte mainIn_uc)
	{
		Integer valeur = null;
		Retour result_dw = Retour.ERROR;
		int msgSize_uw;
		int bcc_uw = 0;
		int k_uw;
		int cptSkip_uw = 0;

		// *** Calage sur le STX
		while (size_uw > 0 && buffer_puc[cptSkip_uw] != STX) {
			size_uw--;
			cptSkip_uw++;
		}

		if (size_uw >= 8) {
			msgSize_uw = ((int) (buffer_puc[cptSkip_uw + TRAME_ISIZE0] + 256) % 256) * 256
					+ (((int) buffer_puc[cptSkip_uw + TRAME_ISIZE1] + 256) % 256) + 3;
			mainId = buffer_puc[cptSkip_uw + 3];
			anzId_uc = buffer_puc[cptSkip_uw + 4];
			cmdId = Type.getEnum(buffer_puc[cptSkip_uw + 5]);

			if ((mainIn_uc != mainId) && (mainIn_uc != 0)) {
				result_dw = Retour.ERROR;
			} else if (size_uw >= msgSize_uw + 2) {
				bcc_uw = 0;
				for (k_uw = 0; k_uw < msgSize_uw; k_uw++) {
					bcc_uw += ((int) buffer_puc[cptSkip_uw + k_uw] + 256) % 256;
				}

				if (buffer_puc[cptSkip_uw + msgSize_uw] == (byte) (bcc_uw >> 8)
						&& buffer_puc[cptSkip_uw + msgSize_uw + 1] == (byte) (bcc_uw & 0xFF)) {
					recopie(buffer_auc, buffer_puc, 0, cptSkip_uw, msgSize_uw);
					this.size_uw = msgSize_uw;
					ready_b = true;
					result_dw = Retour.OK;
				} else {
					result_dw = Retour.ERROR;
				}
			} else {
				result_dw = Retour.INCOMPLETE;
			}
		} else {
			result_dw = Retour.INCOMPLETE;
		}

		if (result_dw == Retour.OK) {
			toString();
			valeur = null;
		} else if (result_dw == Retour.ERROR) {
			valeur = new Integer(-1);
		} else {
			valeur = new Integer(cptSkip_uw);
		}

		/*
		 * On retourne le nombre de caractères que l'on a saute au debut avant
		 * de trouver un STX.
		 */
		/* Cette info est utilisée une fois le message complet trouvé. */
		return valeur;
	}

	public Integer decapsulate ( 
			byte buffer_puc[], 
			int size_uw,
			byte mainId_uc )
	{
		return decapsulateBis(buffer_puc,size_uw,mainId_uc ); // SG 21/05/07
	}
	static void recopie(byte dest[],byte source[],int offset_source,int offset_dest,int taille)
	{
		for (int i = 0; i < taille; i++) {
			dest[i+offset_source]=source[i+offset_dest];

		}
	}

	public Integer extractUint8(int offset_uw )
	{
		Integer success = null;

		if( size_uw > offset_uw ) {
			success = new Integer(((int)buffer_auc[offset_uw]+256)%256);
		}

		return success;
	}

	/**------------------------------------------------------------------------------------------------
	 *
	 */
	public Integer extractUint16(int offset_uw)
	{
		Integer success = null;

		if( size_uw > offset_uw + 1 ) {
			success= new Integer((((int) buffer_auc[offset_uw]+256)%256)*256+
					((int) buffer_auc[offset_uw+1]+256)%256); 
		}

		return success;
	}

	public Long extractUint32(int offset_uw)
	{
		Long success = null;

		if( size_uw > offset_uw + 3 ) {
			success= new Long(
					(((((long) buffer_auc[offset_uw+3]+256)%256)*256+
							((long) buffer_auc[offset_uw+2]+256)%256)*256+
							((long) buffer_auc[offset_uw+1]+256)%256)*256+
							((long) buffer_auc[offset_uw]+256)%256);
		}

		return success;
	}

	public void init(byte mainId_uc, byte anzId_uc, Type cmdId_uc )
	{
		buffer_auc[0] = STX;
		buffer_auc[TRAME_ISIZE0] = 0;
		buffer_auc[TRAME_ISIZE1] = 0;
		buffer_auc[3] = mainId_uc;
		buffer_auc[4] = anzId_uc;
		buffer_auc[5] = (byte)cmdId_uc.value();
		size_uw = 6;
		ready_b = false;
	}

	/**------------------------------------------------------------------------------------------------
	 *
	 */
	public boolean appendBuffer( byte buffer_puc[], int size_uw )
	{
		boolean success_b = false;

		if( !ready_b && ((this.size_uw + size_uw) < TRAME_MAXSIZE) ) {
			recopie(buffer_auc,buffer_puc,this.size_uw,0,size_uw);
			this.size_uw+=size_uw;
			success_b = true;
		}

		return success_b;
	}

	/**------------------------------------------------------------------------------------------------
	 *
	 */
	public boolean appendUint8(int value )
	{
		boolean success_b = false;

		if( !ready_b && ((this.size_uw + 1) < TRAME_MAXSIZE) ) {
			buffer_auc[this.size_uw++] = (byte)value;
			success_b = true;
		}

		return success_b;	
	}
	public boolean appendUint16(int value )
	{
		boolean success_b = false;

		if( !ready_b && ((this.size_uw + 2) < TRAME_MAXSIZE) ) {
			buffer_auc[this.size_uw++] = (byte)(value/256);
			buffer_auc[this.size_uw++] = (byte)(value%256);
			success_b = true;
		}

		return success_b;	
	}
	public boolean appendUint32(int value )
	{
		boolean success_b = false;

		if( !ready_b && ((this.size_uw + 4) < TRAME_MAXSIZE) ) {
			buffer_auc[this.size_uw++] = (byte)(value/(256*256*256));
			buffer_auc[this.size_uw++] = (byte)((value%(256*256*256))/(256*256));
			buffer_auc[this.size_uw++] = (byte)((value%(256*256))/256);
			buffer_auc[this.size_uw++] = (byte)(value%256);
			success_b = true;
		}

		return success_b;	
	}

	/**------------------------------------------------------------------------------------------------
	 *
	 */
	public boolean encapsulate(  )
	{
		boolean success_b = false;
		int bcc_uw = 0;
		int k_uw;
		int msgSize_uw = this.size_uw - 3;

		if(( !ready_b) && (this.size_uw >= 6) ) 
		{
			buffer_auc[TRAME_ISIZE0] = (byte) ( msgSize_uw >> 8 );
			buffer_auc[TRAME_ISIZE1] = (byte) ( msgSize_uw & 0xFF );

			for( k_uw = 0; k_uw < size_uw; k_uw++ ) {
				bcc_uw += ((int)buffer_auc[k_uw]+256)%256;
			}

			buffer_auc[size_uw++] = (byte) ( bcc_uw >> 8 );
			buffer_auc[size_uw++] = (byte) ( bcc_uw & 0xFF );
			ready_b = true;
			success_b = true;
		}

		return success_b;
	}
	public static KsccTrame buildRequestMeasureRead(byte mainId_uc, byte anzId_uc )
	{
		KsccTrame trame=new KsccTrame();
		trame.init(mainId_uc,anzId_uc,Type.CMD_MEASURE_READ);
		trame.encapsulate();
		return trame;
	}
	public byte[] getBuffer()
	{
		byte buffer[]=new byte[this.size_uw];
		for (int i = 0; i < this.size_uw; i++) {
			buffer[i]=this.buffer_auc[i];
		}
		return buffer;
	}
	public boolean isRequest()
	{
	boolean value=false;
		if(cmdId.equals(Type.CMD_MEASURE_READ))
		{
			value=size_uw<=12;
		}
		else if(cmdId.equals(Type.CMD_VERSION_READ))
		{
			value=size_uw<=10;
		}
		return value;
	}
	public boolean getDataAnswerMeasureRead(Vector<Dci> listeDci) 
	{
		boolean error_b = false;
		int offset_uw;
		Integer status[]=new Integer[4];
		Integer nbDci;
		if(size_uw>12)
		{

		status[0]=extractUint8(  7 );
		status[1]=extractUint8(  8 );
		status[2]=extractUint8(  9 );
		status[3]=extractUint8(  10 );
		nbDci=extractUint8(  11 );

		offset_uw = 12;
		for(int k_uc = 0; k_uc < nbDci.intValue() && !error_b; k_uc++ ) {
			Dci dci=extractDci( offset_uw );
			if(null!=dci)
			{
				listeDci.add(dci);
			}
			else
			{
				error_b=true;
			}
			offset_uw+=18;
		}

		}
		else
		{
			error_b=true;
		}
		return !error_b;
	}
	private	Dci extractDci(int offset_uw )
	{
		Dci dci=new Dci();
		if(offset_uw+18<=this.size_uw)
		{
			dci.anzId = extractUint8( offset_uw ).byteValue();
			dci.voie= extractUint8( offset_uw +1).byteValue();
			dci.cal=new GregorianCalendar(
					extractUint8(offset_uw+4).intValue()+2000,
					extractUint8(offset_uw+3).intValue(),
					extractUint8(offset_uw+2).intValue(),
					extractUint8(offset_uw+5).intValue(),
					extractUint8(offset_uw+6).intValue(),
					extractUint8(offset_uw+7).intValue());
			dci.cal.set(GregorianCalendar.MILLISECOND, extractUint16(offset_uw+8).intValue());
			dci.sensc= extractUint8( offset_uw+10).intValue();
			dci.vitesse= extractUint8( offset_uw+11 ).intValue();
			dci.longueur= extractUint8( offset_uw+12 ).intValue();
			dci.tempsPresence= extractUint16( offset_uw+13 ).intValue();
			dci.pl= extractUint8( offset_uw+15 ).intValue();
			dci.silhouette= extractUint8( offset_uw+16 ).intValue();
			dci.confiance= extractUint8( offset_uw+17 ).intValue();
//			System.out.println("Extraction dci "+new SimpleDateFormat("HH:mm:ss.SSS ").format(dci.cal.getTime())+
//					""+extractUint8(offset_uw+7).intValue()+":"+extractUint16(offset_uw+8).intValue());
		}
		else
		{
			dci=null;
		}

		return dci;

	}
	public static KsccTrame buildRequestVersionRead(byte mainId_uc, byte anzId_uc)
	{
		KsccTrame trame=new KsccTrame();
		trame.init(mainId_uc, anzId_uc, Type.CMD_VERSION_READ);
		if(false == trame.encapsulate( ))
		{
			trame=null;
		}
		return trame;
	}
	public static KsccTrame buildRequestTimestampRead(byte mainId_uc, byte anzId_uc)
	{
		KsccTrame trame=new KsccTrame();
		trame.init(mainId_uc, anzId_uc, Type.CMD_TIMESTAMP_READ);
		if(false == trame.encapsulate( ))
		{
			trame=null;
		}
		return trame;
	}

	public boolean getDataAnswerTimestampRead(GregorianCalendar cal)
	{
		GregorianCalendar tmp=new GregorianCalendar(
				extractUint8(8).intValue()+2000,
				extractUint8(7).intValue(),
				extractUint8(6).intValue(),
				extractUint8(10).intValue(),
				extractUint8(11).intValue(),
				extractUint8(12).intValue());
		tmp.set(GregorianCalendar.MILLISECOND, 
				extractUint16(13).intValue());
		cal.setTime(tmp.getTime());
		return true;
	}

	public static KsccTrame buildAnswerTimestampRead(byte mainId_uc, byte anzId_uc,GregorianCalendar cal)
	{
		KsccTrame trame=new KsccTrame();
		trame.init(mainId_uc, anzId_uc, Type.CMD_TIMESTAMP_READ);
		trame.appendUint8(cal.get(GregorianCalendar.DAY_OF_MONTH));
		trame.appendUint8(cal.get(GregorianCalendar.MONTH));
		trame.appendUint8(cal.get(GregorianCalendar.YEAR)%100);
		trame.appendUint8(0xFF);
		trame.appendUint8(cal.get(GregorianCalendar.HOUR_OF_DAY));
		trame.appendUint8(cal.get(GregorianCalendar.MINUTE));
		trame.appendUint8(cal.get(GregorianCalendar.SECOND));
		trame.appendUint16(cal.get(GregorianCalendar.MILLISECOND));

		if(false == trame.encapsulate( ))
		{
			trame=null;
		}
		return trame;
	}
	public boolean getDataAnswerVersionRead(KsccVersion version)
	{
	boolean retour_b=true;
		if(size_uw>10)
		{
		version.soft=extractUint16(6);
		version.hard=extractUint16(8);
		for (int i = 0; i < version.checksum.length; i++) {
			version.checksum[i]=extractUint16(10+2*i);
		}
		}
		else
		{
			retour_b=false;
		}
		return retour_b;
	}
	
	public static KsccTrame buildAnswerVersionRead(byte mainId_uc, byte anzId_uc, 
			int softVersion, int hardVersion, int checksum1, int checksum2,int checksum3,int checksum4)
	{
		KsccTrame trame=new KsccTrame();
		trame.init(mainId_uc, anzId_uc, Type.CMD_VERSION_READ);
		trame.appendUint16( softVersion );
		trame.appendUint16( hardVersion );
		trame.appendUint16( checksum1 );
		trame.appendUint16( checksum2 );
		trame.appendUint16( checksum3 );
		trame.appendUint16( checksum4 );
		if(false == trame.encapsulate( ))
		{
			trame=null;
		}
		return trame;
	}

	public static KsccTrame buildAnswerMeasureRead(byte mainId_uc, byte anzId_uc, 
			int status1_uc, int status2_uc, int status3_uc, int status4_uc, Vector<Dci> listeDci )
	{
		KsccTrame trame=new KsccTrame();
		trame.init(mainId_uc, anzId_uc, Type.CMD_MEASURE_READ);
		trame.appendUint8(0);
		trame.appendUint8(status1_uc);
		trame.appendUint8(status2_uc);
		trame.appendUint8(status3_uc);
		trame.appendUint8(status4_uc);
		trame.appendUint8(listeDci.size());
		for (Dci dci : listeDci) {
			trame.appendUint8(dci.anzId);
			trame.appendUint8(dci.voie);
			trame.appendUint8(dci.cal.get(GregorianCalendar.DAY_OF_MONTH));
			trame.appendUint8(dci.cal.get(GregorianCalendar.MONTH));
			trame.appendUint8(dci.cal.get(GregorianCalendar.YEAR)%100);
			trame.appendUint8(dci.cal.get(GregorianCalendar.HOUR_OF_DAY));
			trame.appendUint8(dci.cal.get(GregorianCalendar.MINUTE));
			trame.appendUint8(dci.cal.get(GregorianCalendar.SECOND));
			trame.appendUint16(dci.cal.get(GregorianCalendar.MILLISECOND));
			System.out.println("Encapsulation dci "+new SimpleDateFormat("HH:mm:ss.SSS ").format(dci.cal.getTime()));

			trame.appendUint8(dci.sensc);
			trame.appendUint8(dci.vitesse);
			trame.appendUint8(dci.longueur);
			trame.appendUint16(dci.tempsPresence);
			trame.appendUint8(dci.pl);
			trame.appendUint8(dci.silhouette);
			trame.appendUint8(dci.confiance);

		}
		if(false == trame.encapsulate( ))
		{
			trame=null;
		}
		return trame;
	}
	public Type getCmdId() {
		return cmdId;
	}
	public void setCmdId(Type cmdId) {
		this.cmdId = cmdId;
	}
	public int getNumCam() {
		// TODO Auto-generated method stub
		return anzId_uc;
	}
	public int getNumAna() {
		// TODO Auto-generated method stub
		return mainId;
	}

}
