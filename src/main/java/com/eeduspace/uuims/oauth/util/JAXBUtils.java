package com.eeduspace.uuims.oauth.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:JAXB工具类
 */
public class JAXBUtils {
    private JAXBUtils() {

        	        throw new UnsupportedOperationException();
            }

           	    public static Object unmarshal(String contextPath, InputStream xmlStream)
               throws JAXBException {

       	        JAXBContext jc = JAXBContext.newInstance(contextPath);
      	        Unmarshaller u = jc.createUnmarshaller();

       	        return u.unmarshal(xmlStream);
       	    }

          	    public static void marshal(String contextPath, Object obj,
                                                      OutputStream stream) throws JAXBException {

               JAXBContext jc = JAXBContext.newInstance(contextPath);
     	        Marshaller m = jc.createMarshaller();
      	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

       	        m.marshal(obj, stream);
          }
    /**
     * JavaBean转换成xml
     * 默认编码UTF-8
     * @param obj
     * @return
     */
    public static String convertToXml(Object obj) {
        return convertToXml(obj, "UTF-8");
    }

    /**
     * JavaBean转换成xml
     * @param obj
     * @param encoding
     * @return
     */
    public static String convertToXml(Object obj, String encoding) {
        String result = null;
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            //决定是否在转换成xml时同时进行格式化（即按标签自动换行，否则即是一行的xml）
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            result = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * xml转换成JavaBean
     * @param xml
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T converyToJavaBean(String xml, Class<T> c) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return t;
    }
}
