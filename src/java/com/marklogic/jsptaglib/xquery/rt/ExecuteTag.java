/*
 * Copyright 2004 Mark Logic Corporation. All Rights Reserved.
 */
package com.marklogic.jsptaglib.xquery.rt;

import com.marklogic.jsptaglib.xquery.common.StatementProperties;
import com.marklogic.jsptaglib.xquery.common.ResultImpl;
import com.marklogic.jsptaglib.xquery.XdbcHelper;
import com.marklogic.jsptaglib.AttributeHelper;
import com.marklogic.xdbc.XDBCConnection;
import com.marklogic.xdbc.XDBCException;
import com.marklogic.xdbc.XDBCResultSequence;
import com.marklogic.xdbc.XDBCStatement;
import com.marklogic.xdmp.XDMPDataSource;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

import java.io.IOException;

/**
 * @jsp:tag name="execute" description="Mark Logic XDBC Statement Tag"
 *  body-content="JSP"
 */
public class ExecuteTag extends BodyTagSupport
	implements StatementProperties, TryCatchFinally
{
	private String var = null;
	private String scope = null;
	private String query = null;
	private String module = null;
	private String separator = null;
	private XDMPDataSource dataSource = null;

	private boolean queryExecuted = false;

	private XDBCStatement xdbcStatement = null;
	private XDBCConnection xdbcConnection = null;

	// -------------------------------------------------------------

	public void release ()
	{
		dataSource = null;
		query = null;
		module = null;
		xdbcStatement = null;
		xdbcConnection = null;
		var = null;
		scope = null;
	}

	/**
	 * @jsp:attribute required="false" rtexprvalue="true"
	 */
	public void setVar (String var) throws JspException
	{
		this.var = var;
	}

	/**
	 * @jsp:attribute required="false" rtexprvalue="true"
	 */
	public void setScope (String scope) throws JspException
	{
		this.scope = scope;
	}

	/**
	 * @jsp:attribute required="false" rtexprvalue="true"
	 */
	public void setQuery (String query) throws JspException
	{
		this.query = query;
	}

	/**
	 * @jsp:attribute required="false" rtexprvalue="true"
	 */
	public void setModule (String module) throws JspException
	{
		this.module = module;
	}

	/**
	 * @jsp:attribute required="false" rtexprvalue="true"
	 */
	public void setSeparator (String separator) throws JspException
	{
		this.separator = separator;
	}

	/**
	 * @jsp:attribute required="false" rtexprvalue="true"
	 */
	public void setDataSource (XDMPDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	protected void setQueryExecuted (boolean queryExecuted)
	{
		this.queryExecuted = queryExecuted;
	}

	// ------------------------------------------------------------------------

	public XDBCResultSequence executeQuery ()
		throws XDBCException
	{
		return (xdbcStatement.executeQuery (query));
	}

	// ------------------------------------------------------------------------

	public int doStartTag() throws JspException
	{
		if ((dataSource != null) && ( ! (dataSource instanceof XDMPDataSource))) {
			throw new JspTagException ("DataSource must instance of XDMPDataSource");
		}

		try {
			xdbcConnection = SetDataSourceTag.getConnection (pageContext, (XDMPDataSource) dataSource, true);
			xdbcStatement = xdbcConnection.createStatement();
		} catch (XDBCException e) {
			throw new JspException ("Cannot create XDBCStatement", e);
		}

		return EVAL_BODY_INCLUDE;
	}

	public int doAfterBody()
		throws JspException
	{
		if (getBodyContent() != null) {
			try {
				getPreviousOut().write (getBodyContent().getString().trim());
			} catch (IOException e) {
				throw new JspException("I/O Exception writing body", e);
			}
		}

		return EVAL_PAGE;
	}

	public int doEndTag()
		throws JspException
	{
		if (queryExecuted) {
			if (var == null) {
				return (EVAL_PAGE);
			}

			throw new JspException ("Cannot use both var attribute and nested result tag");
		}

		try {
			// TODO: Handle "module" attribute being set instead of "query"
			XDBCResultSequence xdbcResultSequence = xdbcStatement.executeQuery (query);

			if (var == null) {
				outputResult (xdbcResultSequence, separator);
			} else {
				setResult (xdbcResultSequence, var);
			}
		} catch (XDBCException e) {
			throw new JspException ("executing query", e);
		}

		return EVAL_PAGE;
	}

	private void setResult (XDBCResultSequence xdbcResultSequence, String var)
		throws JspException
	{
		try {
			AttributeHelper.setScopedAttribute (pageContext, var,
				new ResultImpl (xdbcResultSequence), scope);
		} catch (XDBCException e) {
			throw new JspException ("creating ResultItem object", e);
		}
	}

	private void outputResult (XDBCResultSequence xdbcResultSequence, String separator)
		throws JspException
	{
		try {
			XdbcHelper.concatResult (xdbcResultSequence, pageContext.getOut(), separator);
		} catch (XDBCException e) {
			throw new JspException ("processing result", e);
		} catch (IOException e) {
			throw new JspException ("writing result", e);
		}
	}

	// ------------------------------------------------------------------------

	public void doCatch (Throwable throwable) throws Throwable
	{
		throw throwable;
	}

	public void doFinally ()
	{
		try {
			if (xdbcStatement != null) {
				xdbcStatement.close();
				xdbcStatement = null;
			}

			if (xdbcConnection != null) {
				xdbcConnection.close();
				xdbcConnection = null;
			}
		} catch (XDBCException e) {
			// nothing, ignore it
		}

		release();
	}
}