package com.synflow.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil("TestSecretKey2024ForAES256Encryption!");
    }

    @Test
    void encrypt_returnsNonNullCipherText() {
        String result = encryptionUtil.encrypt("hello world");
        assertThat(result).isNotNull().isNotBlank();
    }

    @Test
    void encrypt_returnsDifferentTextFromPlain() {
        String result = encryptionUtil.encrypt("sensitive data");
        assertThat(result).isNotEqualTo("sensitive data");
    }

    @Test
    void decrypt_reversesEncryption() {
        String plain = "Track record: $2.4B across 45 transactions";
        String encrypted = encryptionUtil.encrypt(plain);
        String decrypted = encryptionUtil.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plain);
    }

    @Test
    void encrypt_producesUniqueCiphertexts() {
        String plain = "same input";
        String enc1 = encryptionUtil.encrypt(plain);
        String enc2 = encryptionUtil.encrypt(plain);
        // GCM uses random IV, so same plaintext produces different ciphertext
        assertThat(enc1).isNotEqualTo(enc2);
        // But both decrypt to the same value
        assertThat(encryptionUtil.decrypt(enc1)).isEqualTo(plain);
        assertThat(encryptionUtil.decrypt(enc2)).isEqualTo(plain);
    }

    @Test
    void encrypt_nullInput_returnsNull() {
        assertThat(encryptionUtil.encrypt(null)).isNull();
    }

    @Test
    void decrypt_nullInput_returnsNull() {
        assertThat(encryptionUtil.decrypt(null)).isNull();
    }

    @Test
    void decrypt_invalidCiphertext_throwsException() {
        assertThatThrownBy(() -> encryptionUtil.decrypt("not-valid-base64-cipher!!"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void encrypt_emptyString_roundTrips() {
        String encrypted = encryptionUtil.encrypt("");
        assertThat(encryptionUtil.decrypt(encrypted)).isEmpty();
    }

    @Test
    void encrypt_unicodeText_roundTrips() {
        String unicode = "Investor: \u00e9l\u00e8ve \u2014 \u00a5100M \u2022 \ud83c\udf0d";
        String encrypted = encryptionUtil.encrypt(unicode);
        assertThat(encryptionUtil.decrypt(encrypted)).isEqualTo(unicode);
    }

    @Test
    void encrypt_longText_roundTrips() {
        String longText = "A".repeat(10000);
        String encrypted = encryptionUtil.encrypt(longText);
        assertThat(encryptionUtil.decrypt(encrypted)).isEqualTo(longText);
    }
}
