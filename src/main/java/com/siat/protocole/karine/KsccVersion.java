package com.siat.protocole.karine;

public class KsccVersion {
	final static int MAX_CKS=4;
	public int soft;
	public int hard;
	public int checksum[]=new int[MAX_CKS];
	public KsccVersion()
	{
		soft=0;
		hard=0;
		for (int i = 0; i < checksum.length; i++) {
			checksum[i]=0;
		}
	}
	public String toString()
	{
		StringBuffer sb=new StringBuffer( "Version soft: "+soft+" hard: "+hard+" (");
		for (int i = 0; i < checksum.length; i++) {
			if(0!=i)
			{
				sb.append("/");
			}
			sb.append(checksum[i]);
		}
		sb.append(")");
		return sb.toString();
	}
}
