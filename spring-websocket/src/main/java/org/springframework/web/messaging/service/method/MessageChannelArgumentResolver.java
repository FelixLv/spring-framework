/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.web.messaging.service.method;

import org.springframework.core.MethodParameter;
import org.springframework.messaging.GenericMessage;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.messaging.PubSubHeaders;
import org.springframework.web.messaging.event.EventBus;
import org.springframework.web.messaging.service.AbstractMessageService;

import reactor.util.Assert;


/**
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class MessageChannelArgumentResolver implements ArgumentResolver {

	private final EventBus eventBus;


	public MessageChannelArgumentResolver(EventBus eventBus) {
		Assert.notNull(eventBus, "reactor is required");
		this.eventBus = eventBus;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return MessageChannel.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {

		final String sessionId = new PubSubHeaders(message.getHeaders(), true).getSessionId();

		return new MessageChannel() {

			@Override
			public boolean send(Message<?> message) {

				PubSubHeaders headers = new PubSubHeaders(message.getHeaders(), false);
				headers.setSessionId(sessionId);
				message = new GenericMessage<Object>(message.getPayload(), headers.getMessageHeaders());

				eventBus.send(AbstractMessageService.CLIENT_TO_SERVER_MESSAGE_KEY, message);

				return true;
			}
		};
	}

}