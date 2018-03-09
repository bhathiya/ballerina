/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.net.http.actions.websocketconnector;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AbstractNativeAction;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.WebSocketConnectionManager;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "closeConnection",
        connectorName = WebSocketConstants.WEBSOCKET_CONNECTOR,
        args = {
                @Argument(name = "c", type = TypeKind.CONNECTOR),
                @Argument(name = "statusCode", type = TypeKind.INT),
                @Argument(name = "reason", type = TypeKind.STRING)
        },
        connectorArgs = {
                @Argument(name = "attributes", type = TypeKind.MAP)
        }
)
public class CloseConnection extends AbstractNativeAction {

    private static final Logger logger = LoggerFactory.getLogger(CloseConnection.class);

    @Override
    public ConnectorFuture execute(Context context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native Action : {}", this.getName());
        }
        ClientConnectorFuture connectorFuture = new ClientConnectorFuture();
        BConnector wsConnector = (BConnector) getRefArgument(context, 0);
        int statusCode = getIntArgument(context, 0);
        String reason = getStringArgument(context, 0);
        Session session = (Session) wsConnector.getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_SESSION);
        try {
            session.close(new CloseReason(() -> statusCode, reason));
            connectorFuture.notifySuccess();
        } catch (Throwable e) {
            connectorFuture.notifyFailure(
                    new BallerinaConnectorException("Could not close the connection: " + e.getMessage()));
        } finally {
            WebSocketConnectionManager.getInstance().removeConnection(session.getId());
        }
        return connectorFuture;
    }
}
