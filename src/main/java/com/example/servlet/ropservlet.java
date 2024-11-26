package com.example.servlet;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.iface.Response;

/**
 * Servlet implementation class HelloServlet
 */
@WebServlet("/ropservice/*")
public class ropservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ropservlet() {
        super();
    }


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String xml = "";
		String infoBinder = "";
		String wsdlOpname = "";
		String wsdlRequest = "";
		String projectFile = "";
		String path = request.getPathInfo();
		if ("/personInfo".equals(path)) {
			xml = (String) request.getAttribute("xmlData");
		    System.out.println("-----Received XML in doGet------: \n" + xml);
	    	infoBinder = "PersonInformation_Entities_External_ws_PersonInformation_WSSP_ASE_Binder";
	    	wsdlOpname = "PersonInformation";
	    	wsdlRequest = "Request1";
            //Dev
        	//projectFile = "C:/Users/prasanth.s/Moshina/ROP-xml-2024-10-30/ROP-uat services/ROP-UAT-soapui-project.xml";
        	//UAT
        	//projectFile = "file:////u01/config/roptestconfig/ROP-UAT-soapui-project.xml";
        	//PROD
        	projectFile = "file:////u01/ropconfig/ROP-PROD-soapui-project.xml";
	    } else if ("/trafficInfo".equals(path)) {
	    	xml = (String) request.getParameter("reqPayload");
	    	System.out.println("reqPayload for Traffic Info: "+xml); 
	    	infoBinder = "TrafficInformation_Entities_ws_provider_TrafficInformation_ASE_Binder";
	    	wsdlOpname = "TrafficInformation";
	    	wsdlRequest = "Request 1";
	    	//projectFile = "C:/Users/prasanth.s/Moshina/ROP-xml-2024-10-30/ROP-uat services/ROP-UAT-soapui-project.xml";
        	projectFile = "file:////u01/ropconfig/ROP-PROD-soapui-project.xml";
	    	//projectFile = "file:////u01/config/roptestconfig/ROP-UAT-soapui-project.xml";
	    } else if ("/getCompanyData".equals(path)) {
	    	xml = (String) request.getAttribute("xmlData");
		    System.out.println("-----Received XML in doGet------: \n" + xml);
	    	infoBinder = "CompanyInformationEndpointV21PortBinding";
	    	wsdlOpname = "getCompanyData";
	    	wsdlRequest = "Request 1";
	    	//Dev
        	//projectFile = "C:/Users/prasanth.s/Moshina/ROP-xml-2024-10-30/ROP-prod services/MOCI-PROD-soapui-project.xml";
        	//UAT
        	//projectFile = "file:////u01/config/roptestconfig/MOCI-PROD-soapui-project.xml";
        	//PROD
        	projectFile = "file:////u01/ropconfig/MOCI-PROD-soapui-project.xml";
	    }
	    else {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path parameter");
	    }
		
        System.out.println("path: "+path);
		StandaloneSoapUICore core = null;
		String result = "";
		String resData = "";
        try {
        	core = new StandaloneSoapUICore(true);
            SoapUI.setSoapUICore(core);

            WsdlProject project = new WsdlProject(projectFile);

            int c = project.getInterfaceCount();

            System.out.println("The interface count   =" + c);

            for (int i = 0; i < c; i++) {
                if(project.getInterfaceAt(i).getName().equals(infoBinder)){
					System.out.println("Interface " + i + ": " + project.getInterfaceAt(i).getName());
					WsdlInterface wsdl = (WsdlInterface) project.getInterfaceAt(i);
					String soapVersion = wsdl.getSoapVersion().toString();
					System.out.println("The SOAP version =" + soapVersion);
					System.out.println("The binding name = " + wsdl.getBindingName());

					int opc = wsdl.getOperationCount();

					System.out.println("Operation count =" + opc);

					for (int j = 0; j < opc; j++) {
						
						WsdlOperation op = wsdl.getOperationAt(j);

						String opName = op.getName();
						if(opName.equals(wsdlOpname)){
						System.out.println("OPERATION:" + opName);
						
						WsdlRequest req = op.getRequestByName(wsdlRequest);
						System.out.println("Request payload is :" + xml);
						//String request = req.getRequestContent();
						req.setRequestContent(xml);
						System.out.println("The request content is =" + req.getRequestContent());

						WsdlSubmitContext wsdlSubmitContext = new WsdlSubmitContext(req);
						WsdlSubmit<?> submit = (WsdlSubmit<?>) req.submit(wsdlSubmitContext, false);

						Response response1 = submit.getResponse();
						//response1.getContentAsXml();

						result = response1.getContentAsString();

						System.out.println("The result =" + result);
						if (!path.equals("/getCompanyData")){
							resData = extractPersonData(result);
						}else {
							resData = result;
						}
				        System.out.println("resData: "+resData);
						
						}
					}
				}
            }
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }finally{
			/*
			 * try { if (core != null) { SoapUI.shutdown(); }
			 * 
			 * // Wait for a short time to allow threads to terminate Thread.sleep(1000);
			 * 
			 * // Interrupt any remaining threads Thread[] threads = new
			 * Thread[Thread.activeCount()]; Thread.enumerate(threads); for (Thread t :
			 * threads) { if (t != null && t.getName().contains("SoapUI")) { t.interrupt();
			 * } } } catch (InterruptedException e) { e.printStackTrace(); } finally { //
			 * Force exit System.exit(0); }
			 */
        }
        response.setContentType("application/xml;charset=UTF-8"); // Set response type to XML
        response.getWriter().write(resData);
        //response.getWriter().append("Served at: ").append(personData);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/xml; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
	    String xml = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
	    System.out.println(" ------POST xml inside doPost method--------- \n" + xml);
	    request.setAttribute("xmlData", xml);
	    doGet(request, response);
	}
	private String extractPersonData(String response) {
		String startTag = "<Response>";
        String endTag = "</Response>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        if (startIndex != -1 && endIndex != -1) {
            // No need to adjust startIndex since we want to include the tag itself
            return response.substring(startIndex, endIndex + endTag.length());
        } else {
            return "No response from target";
        }
	}

}
