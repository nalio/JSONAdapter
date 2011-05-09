/*
 * Copyright (C) 2011 - Progress Software
 */
package com.progress.codeshare.esbservice.json;

import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import com.sonicsw.xq.XQConstants;
import com.sonicsw.xq.XQEnvelope;
import com.sonicsw.xq.XQInitContext;
import com.sonicsw.xq.XQMessage;
import com.sonicsw.xq.XQLog;
import com.sonicsw.xq.XQParameterInfo;
import com.sonicsw.xq.XQParameters;
import com.sonicsw.xq.XQPart;
import com.sonicsw.xq.XQServiceContext;
import com.sonicsw.xq.XQServiceEx;
import com.sonicsw.xq.XQServiceException;

/**
 * @author Ricardo Ferreira (rferreira@progress.com)
 */
public class JSONAdapter implements XQServiceEx {

	// This is the XQLog (the container's logging mechanism).
	private XQLog m_xqLog = null;

	// This is the the log prefix that helps identify this service during
	// logging
	private String m_logPrefix = "";

	// These hold version information.
	private static int s_major = 2009; // year

	private static int s_minor = 915; // month-day

	private static int s_buildNumber = 1637; // hour-minute

	public static final String MESSAGE_PART_PARAM = "messagePart";

	public static final String TRANS_TYPE_PARAM = "transformationType";

	public static final String TEXT_PLAIN_VALUE = "text/plain";

	public static final String TEXT_XML_VALUE = "text/xml";

	private int messagePart;

	private TransformationType transformationType;

	@SuppressWarnings("unchecked")
	private void readParameters(XQParameters xqParameters) {
		Map parametersAllInfo = xqParameters.getAllInfo();
		Iterator iterator = parametersAllInfo.values().iterator();
		while (iterator.hasNext()) {
			XQParameterInfo xqParameterInfo = (XQParameterInfo) iterator.next();
			if (xqParameterInfo.getName().equals(MESSAGE_PART_PARAM)) {
				messagePart = Integer.parseInt(xqParameterInfo.getValue());
			}
			if (xqParameterInfo.getName().equals(TRANS_TYPE_PARAM)) {
				int transType = Integer.parseInt(xqParameterInfo.getValue());
				if (transType == 1) {
					transformationType = TransformationType.XML2JSON;
				} else if (transType == 2) {
					transformationType = TransformationType.JSON2XML;
				}
			}
		}
	}

	/**
	 * Initialize the XQService by processing its initialization parameters.
	 * 
	 * <p>
	 * This method implements a required XQService method.
	 * 
	 * @param initialContext
	 *            The Initial Service Context provides access to:<br>
	 * @exception XQServiceException
	 *                Used in the event of some error.
	 */
	public void init(XQInitContext arg0) throws XQServiceException {

	}

	/**
	 * Initialize the XQService by processing its initialization parameters.
	 * 
	 * <p>
	 * This method implements a required XQService method.
	 * 
	 * @param initialContext
	 *            The Initial Service Context provides access to:<br>
	 * @exception XQServiceException
	 *                Used in the event of some error.
	 */
	//@SuppressWarnings("unchecked")
	public void service(XQServiceContext xqServiceContext)
			throws XQServiceException {
		XQEnvelope xqEnvelope = null;
		XQMessage xqMessage = null;
		XQPart xqPart, newXqPart = null;
		String tempContent = null;
		XMLSerializer xmlSerializer = null;
		JSONObject jsonObject = null;
		JSON json = null;
		try {
			readParameters(xqServiceContext.getParameters());
			xqEnvelope = xqServiceContext.getNextIncoming();
			if (xqEnvelope != null) {
				xqMessage = xqEnvelope.getMessage();
				xqPart = xqMessage.getPart(messagePart);
				newXqPart = xqMessage.createPart();
				tempContent = xqPart.getContent().toString();
				xmlSerializer = new XMLSerializer();
				switch (transformationType) {
				case XML2JSON:
					json = xmlSerializer.read(tempContent);
					tempContent = json.toString(2);
					newXqPart.setContent(tempContent, TEXT_PLAIN_VALUE);
					break;
				case JSON2XML:
					jsonObject = JSONObject.fromObject(tempContent);
					tempContent = xmlSerializer.write(jsonObject);
					newXqPart.setContent(tempContent, TEXT_XML_VALUE);
					break;
				}
				xqMessage.replacePart(newXqPart, messagePart);
				Iterator addresses = xqEnvelope.getAddresses();
				if (addresses.hasNext()) {
					xqServiceContext.addOutgoing(xqEnvelope);
				}
			}
		} catch (Exception ex) {
			throw new XQServiceException(ex);
		}
	}

	/**
	 * Called by the container on container start.
	 * 
	 * <p>
	 * This method implement a required XQServiceEx method.
	 */
	public void start() {
		m_xqLog.logInformation(m_logPrefix + "Starting...");
		m_xqLog.logInformation(m_logPrefix + "Started...");
	}

	/**
	 * Called by the container on container stop.
	 * 
	 * <p>
	 * This method implement a required XQServiceEx method.
	 */
	public void stop() {
		m_xqLog.logInformation(m_logPrefix + "Stopping...");
		m_xqLog.logInformation(m_logPrefix + "Stopped...");
	}

	/**
	 * Clean up and get ready to destroy the service.
	 * 
	 * <p>
	 * This method implement a required XQService method.
	 */
	public void destroy() {
		m_xqLog.logInformation(m_logPrefix + "Destroying...");
		m_xqLog.logInformation(m_logPrefix + "Destroyed...");
	}

	/**
	 * Clean up and get ready to destroy the service.
	 * 
	 */
	protected void setLogPrefix(XQParameters params) {
		String serviceName = params.getParameter(
				XQConstants.PARAM_SERVICE_NAME, XQConstants.PARAM_STRING);
		m_logPrefix = "[ " + serviceName + " ]";
	}

	/**
	 * Provide access to the service implemented version.
	 * 
	 */
	protected String getVersion() {
		return s_major + "." + s_minor + ". build " + s_buildNumber;
	}

	/**
	 * Writes a standard service startup message to the log.
	 */
	protected void writeStartupMessage(XQParameters params) {

		final StringBuffer buffer = new StringBuffer();

		String serviceTypeName = params.getParameter(
				XQConstants.SERVICE_PARAM_SERVICE_TYPE,
				XQConstants.PARAM_STRING);

		buffer.append("\n\n");
		buffer.append("\t\t " + serviceTypeName + "\n ");

		buffer.append("\t\t Version ");
		buffer.append(" " + getVersion());
		buffer.append("\n");

		buffer
				.append("\t\t Copyright (c) 2009, Progress Sonic Software Corporation (Brazil).");
		buffer.append("\n");

		buffer.append("\t\t All rights reserved. ");
		buffer.append("\n");

		m_xqLog.logInformation(buffer.toString());
	}

	/**
	 * Writes parameters to log.
	 */
	protected void writeParameters(XQParameters params) {

		final Map map = params.getAllInfo();
		final Iterator iter = map.values().iterator();

		while (iter.hasNext()) {
			final XQParameterInfo info = (XQParameterInfo) iter.next();

			if (info.getType() == XQConstants.PARAM_XML) {
				m_xqLog.logInformation(m_logPrefix + "Parameter Name =  "
						+ info.getName());
			} else if (info.getType() == XQConstants.PARAM_STRING) {
				m_xqLog.logInformation(m_logPrefix + "Parameter Name = "
						+ info.getName());
			}

			if (info.getRef() != null) {
				m_xqLog.logInformation(m_logPrefix + "Parameter Reference = "
						+ info.getRef());

				// If this is too verbose
				// /then a simple change from logInformation to logDebug
				// will ensure file content is not displayed
				// unless the logging level is set to debug for the ESB
				// Container.
				m_xqLog.logInformation(m_logPrefix
						+ "----Parameter Value Start--------");
				m_xqLog.logInformation("\n" + info.getValue() + "\n");
				m_xqLog.logInformation(m_logPrefix
						+ "----Parameter Value End--------");
			} else {
				m_xqLog.logInformation(m_logPrefix + "Parameter Value = "
						+ info.getValue());
			}
		}
	}

}