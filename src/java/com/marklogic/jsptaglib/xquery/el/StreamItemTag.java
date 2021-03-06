/*
 * Copyright (c)2004 Mark Logic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The use of the Apache License does not indicate that this project is
 * affiliated with the Apache Software Foundation.
 */
package com.marklogic.jsptaglib.xquery.el;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

import javax.servlet.jsp.JspException;

/**
 * @jsp:tag name="streamItem" body-content="empty"
 *  description="Send current element to output as a string"
 * @author Ron Hitchens (ron.hitchens@marklogic.com)
 */
public class StreamItemTag extends com.marklogic.jsptaglib.xquery.rt.StreamItemTag
{
	/**
	 * @jsp:attribute required="false" rtexprvalue="false"
	 */
	public void setBufferSize (String bufferSize) throws JspException
	{
		super.setBufferSize (((Integer) ExpressionEvaluatorManager.evaluate ("bufferSize", bufferSize, Integer.class, this, pageContext)).intValue());
	}
}
