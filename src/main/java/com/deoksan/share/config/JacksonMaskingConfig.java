package com.deoksan.share.config;

import com.deoksan.share.annotation.Masked;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

/**
 * @Masked 어노테이션이 붙은 String 필드를 JSON 직렬화 시 자동 마스킹한다.
 *
 * 적용 예:
 *   "email": "hoony5@gmail.com"  →  "email": "h***@gmail.com"
 *   "phone": "010-1234-5678"     →  "phone": "0**-****-5678"
 *
 * 이메일은 @ 앞 부분만 마스킹하고 도메인은 유지한다 (식별 가능하되 완전 노출 방지).
 */
@Configuration
public class JacksonMaskingConfig {

    @Bean
    public SimpleModule maskedModule() {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(
                    SerializationConfig config,
                    BeanDescription beanDesc,
                    List<BeanPropertyWriter> beanProperties) {

                for (int i = 0; i < beanProperties.size(); i++) {
                    BeanPropertyWriter writer = beanProperties.get(i);
                    // 일반 클래스 필드: writer.getAnnotation()으로 탐지
                    // record 컴포넌트: accessor 메서드에 전파된 어노테이션을 getMember()로 탐지
                    Masked masked = writer.getAnnotation(Masked.class);
                    if (masked == null && writer.getMember() != null) {
                        masked = writer.getMember().getAnnotation(Masked.class);
                    }
                    if (masked != null && writer.getType().getRawClass() == String.class) {
                        beanProperties.set(i, new MaskedPropertyWriter(writer, masked));
                    }
                }
                return beanProperties;
            }
        });
        return module;
    }

    private static class MaskedPropertyWriter extends BeanPropertyWriter {

        MaskedPropertyWriter(BeanPropertyWriter base, Masked masked) {
            super(base);
            @SuppressWarnings("unchecked")
            JsonSerializer<Object> ser = (JsonSerializer<Object>)
                    (JsonSerializer<?>) new MaskedStringSerializer(masked);
            assignSerializer(ser);
        }
    }

    private static class MaskedStringSerializer extends JsonSerializer<String> {

        private final Masked masked;

        MaskedStringSerializer(Masked masked) {
            this.masked = masked;
        }

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            gen.writeString(mask(value));
        }

        private String mask(String value) {
            // 이메일 형식이면 @ 앞 부분만 마스킹
            int atIdx = value.indexOf('@');
            if (atIdx > 0) {
                String local = value.substring(0, atIdx);
                String domain = value.substring(atIdx);
                String maskedLocal = maskGeneral(local);
                return maskedLocal + domain;
            }
            return maskGeneral(value);
        }

        private String maskGeneral(String value) {
            if (value.length() <= masked.prefixLength() + masked.suffixLength()) {
                return String.valueOf(masked.maskChar()).repeat(value.length());
            }
            String prefix = value.substring(0, masked.prefixLength());
            String suffix = value.substring(value.length() - masked.suffixLength());
            int maskLen = value.length() - masked.prefixLength() - masked.suffixLength();
            return prefix + String.valueOf(masked.maskChar()).repeat(maskLen) + suffix;
        }
    }
}
