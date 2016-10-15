package cn.com.akl.zto.api.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

public class ZTOTest {

  private static EndpointReference targetEPR = new EndpointReference("http://api.zto.cn/WebService.asmx?WSDL");

  public static OMElement getPricePayload(String useridStr, String pwdStr, String srtJobNoStr) {
    OMFactory fac = OMAbstractFactory.getOMFactory();
    OMNamespace omNs = fac.createOMNamespace("http://api.zto.cn/", "");

    OMElement method = fac.createOMElement("Search", omNs);
    OMElement userid = fac.createOMElement("Userid", omNs);
    userid.addChild(fac.createOMText(userid, useridStr));

    OMElement pwd = fac.createOMElement("Pwd", omNs);
    pwd.addChild(fac.createOMText(pwd, pwdStr));

    OMElement sn = fac.createOMElement("SrtjobNo", omNs);
    sn.addChild(fac.createOMText(sn, srtJobNoStr));

    method.addChild(userid);
    method.addChild(pwd);
    method.addChild(sn);
    method.build();
    return method;
  }

  public static void main(String[] args) {
    try {
      OMElement getPricePayload = getPricePayload("AKL", "AKL123", "361305921493");
      System.out.println(getPricePayload);

      Options options = new Options();
      options.setTo(targetEPR);
      options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
      options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

      ServiceClient sender = new ServiceClient();
      sender.setOptions(options);

      OMElement result = sender.sendReceive(getPricePayload);
      System.out.println(result);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
