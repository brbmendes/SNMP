import java.net.InetAddress;
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
	public static final String READ_COMMUNITY = "public";
	public static final String WRITE_COMMUNITY= "private";
	public static final int mSNMPVersion =0; // 0 represents SNMP version=1
	public static final String OID_UPS_OUTLET_GROUP1 = ".1.3.6.1.2.1.1.1.0";
	public static final String OID_UPS_BATTERY_CAPACITY= ".1.3.6.1.2.1.1.1.0";
	private static final int SNMP_PORT = 161;

	public static void main(String[] args)
	{
		Scanner s = new Scanner(System.in);
		int operacao = 0;
		
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
			
			operacao = s.nextInt();
			
			switch(operacao)
			{
				case 0:
					break;
				case 1:
					try
					{
						System.out.println("Operação escolhida: GET");
						String strIPAddress = "127.0.0.1";
						SNMP objSNMP = new SNMP();
						//////////////////////////////////////////////////////////
						//Get Basic state of UPS
						/////////////////////////////////////////////////////////
						String batteryCap =objSNMP.snmpGet(strIPAddress, READ_COMMUNITY, OID_UPS_BATTERY_CAPACITY);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case 2:
					System.out.println("Operação escolhida: GETNEXT");
					break;
				case 3:
					try
					{
						System.out.println("Operação escolhida: SET");
						String strIPAddress = "127.0.0.1";
						SNMP objSNMP = new SNMP();
						//objSNMP.snmpSet(null, null, null, 0); 
						///////////////////////////////////////////
						//Set Value=2 to trun OFF UPS OUTLET Group1
						//Value=1 to trun ON UPS OUTLET Group1
						//////////////////////////////////////////
	
						int Value = 2;
						objSNMP.snmpSet(strIPAddress, WRITE_COMMUNITY, OID_UPS_OUTLET_GROUP1, Value);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case 4:
					System.out.println("Operação escolhida: GETBULK");
					break;
				case 5:
					System.out.println("Operação escolhida: WALK");
					break;
				case 6:
					System.out.println("Operação escolhida: GETTABLE");
					break;
				case 7:
					System.out.println("Operação escolhida: GETDELTA");
					break;
			}	
			
		}
		System.out.println("Encerrando o programa");
		
		
		
		
		System.exit(0);
	}

	


	/*
	 * The following code valid only SNMP version1. This
	 * method is very useful to set a parameter on remote device.
	 */
	public void snmpSet(String strAddress, String community, String strOID, int Value)
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
			pdu.add(new VariableBinding(new OID(strOID), new Integer32(Value)));
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
	/*
	 * The code is valid only SNMP version1. SnmpGet method
	 * return Response for given OID from the Device.
	 */
	public String snmpGet(String strAddress, String community, String strOID)
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
			pdu.add(new VariableBinding(new OID(strOID)));
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
}