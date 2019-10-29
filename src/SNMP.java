import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

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
	//Parâmetros gerais
	public static final int mSNMPVersion =0; // 0 represents SNMP version=1
	private static final int SNMP_PORT = 161;
	
	// Variáveis globais
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
			
			System.out.println("Escolha a operação desejada:");
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
						System.out.println("Operação escolhida: GET");
						ObterEntradasGet();
						_objSNMP = new SNMP();
						_retorno = _objSNMP.snmpGet(_ip, _comunidade, _oid, _instancia);
						System.out.println(_retorno);
						System.out.println("");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case 2: // GETNEXT
					System.out.println("Operação escolhida: GETNEXT");
					ObterEntradasGetNext();
					_objSNMP = new SNMP();
					_retorno = _objSNMP.snmpGetNext(_ip,_comunidade, _oid, _instancia);
					System.out.println(_retorno);
					System.out.println("");
					break;
				case 3: // SET
					try
					{
						System.out.println("Operação escolhida: SET");
						ObterEntradasSet();
						_objSNMP = new SNMP();
						int valor = Integer.MIN_VALUE;
						if(_tipo.equals("int")) {
							valor = Integer.valueOf(_valor);
							_valor = null;
						}
						_objSNMP.snmpSet(_ip, _comunidade, _oid, _instancia, _valor, valor);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case 4: // GETBULK
					System.out.println("Operação escolhida: GETBULK");
					ObterEntradasGetBulk();
					int nonRepeaters = Integer.valueOf(_nonRepeaters);
					int maxRepetitions = Integer.valueOf(_maxRepetitions);
					_retorno = _objSNMP.snmpGetBulk(_ip,_comunidade, _oid, _instancia, nonRepeaters, maxRepetitions);
					System.out.println(_retorno);
					System.out.println("");
					break;
				case 5: // WALK
					System.out.println("Operação escolhida: WALK");
					break;
				case 6: // GETTABLE
					System.out.println("Operação escolhida: GETTABLE");
					break;
				case 7: // GETDELTA
					System.out.println("Operação escolhida: GETDELTA");
					ObterEntradasGetDelta();
					int amostras = Integer.valueOf(_amostras);
					int tempo = Integer.valueOf(_tempo);
					ArrayList<String> resultados  = new ArrayList<>();
					for(int i = 0 ; i < amostras ; i++) {
						_retorno = _objSNMP.snmpGet(_ip, _comunidade, _oid, _instancia);
						resultados.add(0, _retorno);
						Thread.sleep(tempo);
					}
					// Delta = B - A
					for(int i = 0 ; i < resultados.size() - 1 ; i++) {
						int B = Integer.valueOf(resultados.get(i));
						int A = Integer.valueOf(resultados.get(i+1));
						int delta = B - A;
						System.out.println(String.format("O delta entre %d e %d eh %d\n",B, A, delta));
					}
					break;
			}	
			
		}
		System.out.println("Encerrando o programa");
		System.exit(0);
	}

	
	
	/*
	 * The code is valid only SNMP version1. SnmpGet method
	 * return Response for given OID from the Device.
	 */
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
			comtarget.setVersion(SnmpConstants.version1);
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
		System.out.println("Response="+str);
		return str;
	}
	
	public String snmpGetNext(String strAddress, String community, String strOID, String strInstancia)
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
			comtarget.setVersion(SnmpConstants.version1);
			comtarget.setAddress(targetaddress);
			comtarget.setRetries(2);
			comtarget.setTimeout(5000);
			
			PDU pdu = new PDU();
			ResponseEvent response;
			Snmp snmp;
			pdu.add(new VariableBinding(new OID(strOID + "." + strInstancia)));
			pdu.setType(PDU.GETNEXT);
			snmp = new Snmp(transport); 
			response = snmp.get(pdu,comtarget);
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
		System.out.println("Response="+str);
		return str;
	}
	
	/*
	 * The following code valid only SNMP version1. This
	 * method is very useful to set a parameter on remote device.
	 */
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
			target.setVersion(SnmpConstants.version1);

			PDU pdu = new PDU();
			// Se for igual a MinValue, então o valor passado é uma string
			if(intValor == Integer.MIN_VALUE) {
				pdu.add(new VariableBinding(new OID(strOID + "." + strInstancia), _valor));
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
					System.out.println("Set Status is:"+event.getResponse().getErrorStatusText());
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
	
	public String snmpGetBulk(String strAddress, String community, String strOID, String strInstancia, int nonRepeaters, int maxRepetitions)
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
			comtarget.setVersion(SnmpConstants.version1);
			comtarget.setAddress(targetaddress);
			comtarget.setRetries(2);
			comtarget.setTimeout(5000);
			
			PDU pdu = new PDU();
			ResponseEvent response;
			Snmp snmp;
			pdu.add(new VariableBinding(new OID(strOID + "." + strInstancia)));
			pdu.setType(PDU.GETBULK);
			pdu.setNonRepeaters(nonRepeaters);
			pdu.setMaxRepetitions(maxRepetitions);
			snmp = new Snmp(transport); 
			response = snmp.get(pdu,comtarget);
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
		System.out.println("Response="+str);
		return str;
	}
	
	private static void ObterEntradasGet() {
		Scanner s = new Scanner(System.in);
		System.out.println("Informe o IP no formato xxx.xxx.xxx.xxx");
		_ip = s.nextLine();
		
		System.out.println("Informe a comunidade: public ou private");
		_comunidade = s.nextLine();
		
		System.out.println("Informe a OID");
		_oid = s.nextLine();
		
		System.out.println("Informe a instância, sem o ponto(.)");
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
		
		System.out.println("Informe a instância, sem o ponto(.) ou null para não informar a instancia");
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
		
		System.out.println("Informe a instância, sem o ponto(.)");
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
		
		System.out.println("Informe a OID");
		_oid = s.nextLine();
		
		System.out.println("Informe a instância, sem o ponto(.)");
		_instancia = s.nextLine();
		
		System.out.println("Informe a quantidade de Non Repeaters");
		_nonRepeaters = s.nextLine();
		
		System.out.println("Informe a quantidade de Max Repetitions");
		_maxRepetitions = s.nextLine();
	}
	
	private static void ObterEntradasGetDelta() {
		Scanner s = new Scanner(System.in);
		System.out.println("Informe o IP no formato xxx.xxx.xxx.xxx");
		_ip = s.nextLine();
		
		System.out.println("Informe a comunidade: public ou private");
		_comunidade = s.nextLine();
		
		System.out.println("Informe a OID");
		_oid = s.nextLine();
		
		System.out.println("Informe a instância, sem o ponto(.)");
		_instancia = s.nextLine();
		
		System.out.println("Informe a quantidade de amostras");
		_amostras = s.nextLine();
		
		System.out.println("Informe o intervalo de tempo entre as amostras, em segundos");
		_tempo = s.nextLine();
	}
}