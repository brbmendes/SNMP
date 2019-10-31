import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.stream.Collectors;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMP
{
	private static final int SNMP_PORT = 161;
	
	// Variaveis globais
	public static String _ip;
	public static String _comunidade;
	public static String _oid;
	public static String _instancia;
	public static String _tipo;
	public static String _valor;
	public static String _nonRepeaters;
	public static String _maxRepetitions;
	public static String _tempo;
	public static String _amostras;
	public static String _retorno;
	public static SNMP _objSNMP;
	
	public static void main(String[] args) throws InterruptedException
	{
		Scanner ss = new Scanner(System.in);
		int operacao = -1;
		
		while(operacao != 0){
			
			System.out.println("Escolha a operacao desejada:");
			System.out.println("1 - GET");
			System.out.println("2 - GETNEXT");
			System.out.println("3 - SET");
			System.out.println("4 - GETBULK");
			System.out.println("5 - WALK");
			System.out.println("6 - GETTABLE");
			System.out.println("7 - GETDELTA");
			System.out.println("0 - Encerrar programa.");
				
			operacao = Integer.parseInt(ss.nextLine());	
			
			switch(operacao)
			{
				case 0:
					break;
				case 1: // GET
					try
					{
						System.out.println("Operacao escolhida: GET");
						ObterEntradasGet();
						_objSNMP = new SNMP();
						_objSNMP.snmpGet(_ip, _comunidade, _oid, _instancia);
						System.out.println("");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case 2: // GETNEXT
					System.out.println("Operacao escolhida: GETNEXT");
					ObterEntradasGetNext();
					_objSNMP = new SNMP();
					_objSNMP.snmpGetNext(_ip,_comunidade, _oid, _instancia);
					System.out.println("");
					break;
				case 3: // SET
					try
					{
						System.out.println("Operacao escolhida: SET");
						ObterEntradasSet();
						_objSNMP = new SNMP();
						int valor = Integer.MIN_VALUE;
						if(_tipo.equals("int")) {
							valor = Integer.valueOf(_valor);
							_valor = null;
						}
						_objSNMP.snmpSet(_ip, _comunidade, _oid, _instancia, _valor, valor);
						System.out.println("");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case 4: // GETBULK
					System.out.println("Operacao escolhida: GETBULK");
					ObterEntradasGetBulk();					
					int nonRepeaters = Integer.valueOf(_nonRepeaters);
					int maxRepetitions = Integer.valueOf(_maxRepetitions);
					_objSNMP = new SNMP();
					_objSNMP.snmpGetBulk(_ip,_comunidade, _oid, _instancia, nonRepeaters, maxRepetitions);
					System.out.println("");
					break;
				case 5: // WALK
					try
					{
						System.out.println("Operacao escolhida: WALK");
						ObterEntradasWalk(); 
						_objSNMP = new SNMP();
						OID _oid2 = new OID(_oid);
						List<VariableBinding>_retorno2 = SNMP.snmpWalk(_ip, _comunidade, _oid2);
						for(int i=0 ; i<_retorno2.size(); i++){
						    System.out.println(_retorno2.get(i));
						}
						System.out.println("");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case 6: // GETTABLE
					try
					{
						System.out.println("Operacao escolhida: TABLE");
						ObterEntradasWalk(); 
						_objSNMP = new SNMP();
						OID _oid2 = new OID(_oid);
						List<String>_retorno2 = SNMP.snmpGetTable(_ip, _comunidade, _oid2);
						ArrayList<String> colunas = new ArrayList();
						String[] tmpRetorno = new String[2];
						HashMap<String,ArrayList<String>> dict = new HashMap<String, ArrayList<String>>(); 
						for(int i=0 ; i<_retorno2.size(); i++){
						    tmpRetorno = _retorno2.get(i).split("#");
						    if(!colunas.contains(tmpRetorno[0])) {
						    	colunas.add(tmpRetorno[0]);
						    	if(dict.containsKey(tmpRetorno[0])) {
						    		ArrayList<String> tmp = dict.get(tmpRetorno[0]);
						    		if(tmpRetorno.length == 2) {
						    			tmp.add(tmpRetorno[1]);
							    		dict.replace(tmpRetorno[0],tmp);
						    		} else {
						    			tmp.add("null");
							    		dict.replace(tmpRetorno[0],tmp);
						    		}
						    		
						    	} else {
						    		ArrayList<String> tmp = new ArrayList();
						    		if(tmpRetorno.length == 2) {
						    			tmp.add(tmpRetorno[1]);
							    		dict.put(tmpRetorno[0],tmp);
						    		} else {
						    			tmp.add("null");
							    		dict.put(tmpRetorno[0],tmp);
						    		}
						    	}
						    } else {
						    	if(dict.containsKey(tmpRetorno[0])) {
						    		ArrayList<String> tmp = dict.get(tmpRetorno[0]);
						    		if(tmpRetorno.length == 2) {
						    			tmp.add(tmpRetorno[1]);
							    		dict.replace(tmpRetorno[0],tmp);
						    		} else {
						    			tmp.add("null");
							    		dict.replace(tmpRetorno[0],tmp);
						    		}
						    	} else {
						    		ArrayList<String> tmp = new ArrayList();
						    		if(tmpRetorno.length == 2) {
						    			tmp.add(tmpRetorno[1]);
							    		dict.put(tmpRetorno[0],tmp);
						    		} else {
						    			tmp.add("null");
							    		dict.put(tmpRetorno[0],tmp);
						    		}
						    	}
						    }
						}
						
						
						
						Iterator<String> itr = dict.keySet().iterator();				
						while(itr.hasNext()) {
							String key = itr.next();
							ArrayList<String> values = dict.get(key);
							StringBuilder sb = new StringBuilder();
							sb.append(key + ": ");
							for(String val : values) {
								sb.append(String.format("%1$" + 20 + "s", val) + " ");
							}
							System.out.println(sb.toString());
						}
						
						
						
						
						System.out.println("");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case 7: // GETDELTA
					System.out.println("Operacao escolhida: GETDELTA");
					ObterEntradasGetDelta();
					int amostras = Integer.valueOf(_amostras);
					int tempo = Integer.valueOf(_tempo);
					ArrayList<String> resultados  = new ArrayList<>();
					for(int i = 0 ; i < amostras ; i++) {
						_objSNMP = new SNMP();
						_retorno = _objSNMP.snmpGet(_ip, _comunidade, _oid, _instancia);
						resultados.add(0, _retorno);
						Thread.sleep(tempo*1000);
					}
					// Delta = B - A
					System.out.println("\n");
					for(int i = 0 ; i < resultados.size() - 1 ; i++) {
						int B = Integer.valueOf(resultados.get(i).trim());
						int A = Integer.valueOf(resultados.get(i+1).trim());
						int delta = B - A;
						System.out.println(String.format("O delta entre %d e %d = %d\n",B, A, delta));
					}
					System.out.println("");
					break;
			}	
			
		}
		System.out.println("Encerrando o programa");
		System.exit(0);
	}

	
	public String snmpGet(String strAddress, String community, String strOID, String strInstancia)
	{
		String str="";
		try
		{
			OctetString comunidade = new OctetString(community);
			strAddress= strAddress+"/"+SNMP_PORT;
			Address targetaddress = new UdpAddress(strAddress);
			TransportMapping transport = new DefaultUdpTransportMapping();
			transport.listen();

			CommunityTarget comtarget = new CommunityTarget();
			comtarget.setCommunity(comunidade);
			comtarget.setVersion(SnmpConstants.version2c);
			comtarget.setAddress(targetaddress);
			comtarget.setRetries(2);
			comtarget.setTimeout(5000);

			PDU pdu = new PDU();
			ResponseEvent response;
			Snmp snmp;
			pdu.add(new VariableBinding(new OID(strOID + "." + strInstancia)));
			pdu.setType(PDU.GET);
			snmp = new Snmp(transport); 
			response = snmp.get(pdu,comtarget);
			if(response != null)
			{
				if(response.getResponse().getErrorStatusText().equalsIgnoreCase("Success"))
				{
					PDU pduresponse=response.getResponse();
					str=pduresponse.getVariableBindings().firstElement().toString();
					System.out.println(str.toString());
					if(str.contains("="))
					{
						int len = str.indexOf("=");
						str=str.substring(len+1, str.length());
						
					}
				}
			}
			else
			{
				System.out.println("Feeling like a TimeOut occured ");
			}
			snmp.close();
		} catch(Exception e) { 
			e.printStackTrace(); 
		}
		return str;
	}
	
	
	public void snmpGetNext(String strAddress, String community, String strOID, String strInstancia)
	{
		String instancia = strInstancia;
		if(strInstancia.equals("null")) {
			instancia = "0";
		}
		
		String str="";
		try
		{
			OctetString comunidade = new OctetString(community);
			strAddress= strAddress+"/"+SNMP_PORT;
			Address targetaddress = new UdpAddress(strAddress);
			TransportMapping transport = new DefaultUdpTransportMapping();
			transport.listen();

			CommunityTarget comtarget = new CommunityTarget();
			comtarget.setCommunity(comunidade);
			comtarget.setVersion(SnmpConstants.version2c);
			comtarget.setAddress(targetaddress);
			comtarget.setRetries(2);
			comtarget.setTimeout(5000);
			
			PDU pdu = new PDU();
			ResponseEvent response;
			Snmp snmp;
			pdu.add(new VariableBinding(new OID(strOID + "." + instancia)));
			pdu.setType(PDU.GETNEXT);
			snmp = new Snmp(transport); 
			response = snmp.getNext(pdu,comtarget);
			if(response != null)
			{
				if(response.getResponse().getErrorStatusText().equalsIgnoreCase("Success"))
				{
					PDU pduresponse=response.getResponse();
					str=pduresponse.getVariableBindings().firstElement().toString();
					if(str.contains("="))
					{
						int len = str.indexOf("=");
						str=str.substring(len+1, str.length());
						System.out.println(str.toString());
					}
				}
			}
			else
			{
				System.out.println("Feeling like a TimeOut occured ");
			}
			snmp.close();
		} catch(Exception e) { 
			e.printStackTrace(); 
		}
	}
	
	
	public void snmpSet(String strAddress, String community, String strOID, String strInstancia, String strValor, int intValor)
	{
		strAddress= strAddress+"/"+SNMP_PORT;
		Address targetAddress = GenericAddress.parse(strAddress);
		Snmp snmp;
		try
		{
			TransportMapping transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			transport.listen();
			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(community));
			target.setAddress(targetAddress);
			target.setRetries(2);
			target.setTimeout(5000);
			target.setVersion(SnmpConstants.version2c);

			PDU pdu = new PDU();
			// Se for igual a MinValue, entao o valor passado e uma string
			if(intValor == Integer.MIN_VALUE) {
				Variable var = new OctetString(_valor);
				pdu.add(new VariableBinding(new OID(strOID + "." + strInstancia), var));
			} else {
				pdu.add(new VariableBinding(new OID(strOID + "." + strInstancia), new Integer32(intValor)));
			}
			
			pdu.setType(PDU.SET);

			ResponseListener listener = new ResponseListener() { 

				public void onResponse(ResponseEvent event) {
					// Always cancel async request when response has been received
					// otherwise a memory leak is created! Not canceling a request
					// immediately can be useful when sending a request to a broadcast
					// address.
					((Snmp)event.getSource()).cancel(event.getRequest(), this);
					System.out.println("Status do set: "+event.getResponse().getErrorStatusText());
				}
			};
			snmp.send(pdu, target, null, listener);
			snmp.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void snmpGetBulk(String strAddress, String community, String strOID, String strInstancia, int nonRepeaters, int maxRepetitions)
	{
		
		String[] oids = strOID.split("#");
		VariableBinding[] array = new VariableBinding[nonRepeaters + (maxRepetitions)];
		VariableBinding vb = null;
		
		for(int i = 0 ; i < oids.length ; i++) {
			vb = new VariableBinding(new OID(oids[i]));
			array[i] = vb;
		}

		String str="";
		try
		{
			OctetString comunidade = new OctetString(community);
			strAddress= strAddress+"/"+SNMP_PORT;
			Address targetaddress = new UdpAddress(strAddress);
			TransportMapping transport = new DefaultUdpTransportMapping();
			transport.listen();

			CommunityTarget comtarget = new CommunityTarget();
			comtarget.setCommunity(comunidade);
			comtarget.setVersion(SnmpConstants.version2c);
			comtarget.setAddress(targetaddress);
			comtarget.setRetries(2);
			comtarget.setTimeout(5000);
			
			PDU pdu = new PDU();
			ResponseEvent response;
			Snmp snmp;
			pdu.addAll(array);
			pdu.setType(PDU.GETBULK);
			pdu.setNonRepeaters(nonRepeaters);
			pdu.setMaxRepetitions(maxRepetitions);
			snmp = new Snmp(transport); 
			response = snmp.getBulk(pdu,comtarget);
			if(response != null)
			{
				if(response.getResponse().getErrorStatusText().equalsIgnoreCase("Success"))
				{
					PDU pduresponse=response.getResponse();
					Vector<? extends VariableBinding> vbs = pduresponse.getVariableBindings();
		               for (VariableBinding vbres : vbs) {
		                   System.out.println(vbres.getOid().toString() + ": " + vbres.getVariable().toString());
			        }
				}
			}
			else
			{
				System.out.println("Feeling like a TimeOut occured ");
			}
			snmp.close();
		} catch(Exception e) { 
			e.printStackTrace(); 
		}
	}
	
	
	public static List<VariableBinding> snmpWalk(String strAddress, String community, OID strOID) throws IOException
	{
		
		List<VariableBinding> ret = new ArrayList<VariableBinding>();
		OID strOIDAux = null;
		
		OctetString comunidade = new OctetString(community);
		strAddress= strAddress+"/"+SNMP_PORT;
		Address targetaddress = new UdpAddress(strAddress);
		TransportMapping transport = new DefaultUdpTransportMapping();
		transport.listen();

		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(comunidade);
		comtarget.setVersion(SnmpConstants.version2c);
		comtarget.setAddress(targetaddress);
		comtarget.setRetries(2);
		comtarget.setTimeout(5000);
			
			
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(strOID)));
		pdu.setType(PDU.GETNEXT);
		Snmp snmp;
		snmp = new Snmp(transport); 
		ResponseEvent response;
		boolean finished = false;
		
		try
		{	// OID Auxiliar para não realizar "trim" nas pastas raízes cujo possuem tamanho 7.
			// O "trim" é realizado somente em OID maiores que 7 que são objetos dentro dessas raízes.
			// Isso porque se eu diminuir o OID.size() em -1 numa pasta raizes ele vai mostrar tudo de tudo e não somente os itens daquela pasta.
			// Já na questão dos itens dentro da pasta eu posso diminuir -1 (trim) pois eu necessito varrer aquela pasta em questão.
			if (strOID.size() > 7) {
            	strOIDAux = strOID.trim();
            }
			else {
				strOIDAux = strOID;
			}
			
			// Enquanto estᡮa mesma sub-arvore ele continua adicionando na lista os itens.
			while (!finished) {
	            VariableBinding vb = null;

	            response = snmp.send(pdu, comtarget);
	            PDU responsePDU = response.getResponse();
	            if (responsePDU != null) {
	                vb = responsePDU.get(0);
	            } 
	            
	            // Todos os testes para verificar o fim da sub-arvore.
	            // O "leftMostCompare" foi onde usei o OIDAux para o programa n䯠se perder na varredura do walk.
	            // O "leftMostCompare" recebe como parametro um numero inteiro (tamanho do OID) e compara esses primeiros "n"
	            // dtos do OID inserido e do que estᡳendo an⭩sado, se forem iguais ele retorna zero, e se forem iguais no caso
	            // ele ainda estᡮa mesma subarvore sendo assim continua o processo e s󠰡ra quando forem diferentes (mudou a sub-arvore)
	            if (pdu.getErrorStatus() != 0) {
	    			finished = true;
	    		} else if (vb.getOid() == null) {
	    			finished = true;
	    		} else if (vb.getOid().size() < strOID.size()) {
	    			finished = true;
	    		} else if (strOID.leftMostCompare(strOIDAux.size(), vb.getOid()) != 0) {
	    			finished = true;
	    		} else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {

	    			finished = true;
	    		} else if (vb.getOid().compareTo(strOID) <= 0) {
	    			finished = true;
	            } else {
	                ret.add(vb);

	                // Set up the variable binding for the next entry.
	                pdu.setRequestID(new Integer32(0));
	                pdu.set(0, vb);
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		//Retorno da lista com todos os itens do Walk.
	    return ret;
	}
	
	public static List<String> snmpGetTable(String strAddress, String community, OID strOID) throws IOException
	{
		
		List<String> ret = new ArrayList<String>();
		
		OctetString comunidade = new OctetString(community);
		strAddress= strAddress+"/"+SNMP_PORT;
		Address targetaddress = new UdpAddress(strAddress);
		TransportMapping transport = new DefaultUdpTransportMapping();
		transport.listen();

		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(comunidade);
		comtarget.setVersion(SnmpConstants.version2c);
		comtarget.setAddress(targetaddress);
		comtarget.setRetries(2);
		comtarget.setTimeout(5000);
			
			
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(strOID)));
		pdu.setType(PDU.GETNEXT);
		Snmp snmp;
		snmp = new Snmp(transport); 
		ResponseEvent response;
		boolean finished = false;
		
		try
		{	
			
			// Enquanto estᡮa mesma sub-arvore ele continua adicionando na lista os itens.
			while (!finished) {
	            VariableBinding vb = null;

	            response = snmp.send(pdu, comtarget);
	            PDU responsePDU = response.getResponse();
	            if (responsePDU != null) {
	                vb = responsePDU.get(0);
	            } 
	            
	            if (strOID.leftMostCompare(strOID.size(), vb.getOid()) != 0) {
	    			finished = true;
	    		} 
	            else {
	            	String valor = vb.getVariable().toString();
	            	String oid = vb.getOid().toString().substring(0,20);
	                ret.add(oid + "#" + valor);

	                // Set up the variable binding for the next entry.
	                pdu.setRequestID(new Integer32(0));
	                pdu.set(0, vb);
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		//Retorno da lista com todos os itens do Walk.
	    return ret;
	}
	
	private static void ObterEntradasGet() {
		Scanner s = new Scanner(System.in);
		System.out.println("Informe o IP no formato xxx.xxx.xxx.xxx");
		_ip = s.nextLine();
		
		System.out.println("Informe a comunidade: public ou private");
		_comunidade = s.nextLine();
		
		System.out.println("Informe a OID");
		_oid = s.nextLine();
		
		System.out.println("Informe a instancia, sem o ponto(.)");
		_instancia = s.nextLine();
	}
	
	private static void ObterEntradasGetNext() {
		Scanner s = new Scanner(System.in);
		System.out.println("Informe o IP no formato xxx.xxx.xxx.xxx");
		_ip = s.nextLine();
		
		System.out.println("Informe a comunidade: public ou private");
		_comunidade = s.nextLine();
		
		System.out.println("Informe a OID");
		_oid = s.nextLine();
		
		System.out.println("Informe a instancia, sem o ponto(.) ou null para nao informar a instancia");
		_instancia = s.nextLine();
	}
	
	private static void ObterEntradasSet() {
		Scanner s = new Scanner(System.in);
		System.out.println("Informe o IP no formato xxx.xxx.xxx.xxx");
		_ip = s.nextLine();
		
		System.out.println("Informe a comunidade: public ou private");
		_comunidade = s.nextLine();
		
		System.out.println("Informe a OID");
		_oid = s.nextLine();
		
		System.out.println("Informe a instancia, sem o ponto(.)");
		_instancia = s.nextLine();
		
		System.out.println("Informe o tipo de dado: string ou int");
		_tipo = s.nextLine();
		
		System.out.println("Informe o valor");
		_valor = s.nextLine();
	}
	
	private static void ObterEntradasGetBulk() {
		Scanner s = new Scanner(System.in);
		System.out.println("Informe o IP no formato xxx.xxx.xxx.xxx");
		_ip = s.nextLine();
		
		System.out.println("Informe a comunidade: public ou private");
		_comunidade = s.nextLine();
		
		System.out.println("Informe a quantidade de Non Repeaters");
		_nonRepeaters = s.nextLine();
		
		System.out.println("Informe a quantidade de Max Repetitions");
		_maxRepetitions = s.nextLine();
		
		System.out.println("Informe as OIDs separadas por #");
		_oid = s.nextLine();
	}
	
	private static void ObterEntradasWalk() {
		Scanner s = new Scanner(System.in);
		System.out.println("Informe o IP no formato xxx.xxx.xxx.xxx");
		_ip = s.nextLine();
		
		System.out.println("Informe a comunidade: public ou private");
		_comunidade = s.nextLine();
		
		System.out.println("Informe a OID");
		_oid = s.nextLine();
		
	}
	
	private static void ObterEntradasGetTable() {
		Scanner s = new Scanner(System.in);
		System.out.println("Informe o IP no formato xxx.xxx.xxx.xxx");
		_ip = s.nextLine();
		
		System.out.println("Informe a comunidade: public ou private");
		_comunidade = s.nextLine();
		
		System.out.println("Informe a OID da Table");
		_oid = s.nextLine();
		
	}
	
	private static void ObterEntradasGetDelta() {
		Scanner s = new Scanner(System.in);
		System.out.println("Informe o IP no formato xxx.xxx.xxx.xxx");
		_ip = s.nextLine();
		
		System.out.println("Informe a comunidade: public ou private");
		_comunidade = s.nextLine();
		
		System.out.println("Informe a OID");
		_oid = s.nextLine();
		
		System.out.println("Informe a instancia, sem o ponto(.)");
		_instancia = s.nextLine();
		
		System.out.println("Informe a quantidade de amostras");
		_amostras = s.nextLine();
		
		System.out.println("Informe o intervalo de tempo entre as amostras, em segundos");
		_tempo = s.nextLine();
	}
}
