package com.ss.heartlinkapi.sms.util;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;

@Component
public class SmsUtil {
	
	@Value("${coolsms.api.key}")
	private String apiKey;
	
	@Value("${coolsms.api.secret}")
	private String apiSecret;
	
	@Value("${coolsms.api.number}")
	private String fromPhoneNumber;
	
    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }
	
	public SingleMessageSentResponse sendOne(String phone,String verificationCode) {
		
		Message message = new Message();
		message.setFrom(fromPhoneNumber);
		message.setTo(phone);
		message.setText("[하트링크] 본인확인 인증번호는 "+verificationCode+" 입니다.");
		
		SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
		
		return response;
	}
	
	
}
