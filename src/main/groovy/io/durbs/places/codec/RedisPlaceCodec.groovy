package io.durbs.places.codec

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Charsets
import com.lambdaworks.redis.codec.RedisCodec
import groovy.transform.CompileStatic
import io.durbs.places.domain.Place

import java.nio.ByteBuffer

@CompileStatic
class RedisPlaceCodec implements RedisCodec<String, Place> {

  static ObjectMapper objectMapper = new ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

  @Override
  String decodeKey(final ByteBuffer bytes) {

    Charsets.UTF_8.decode(bytes).toString()
  }

  @Override
  Place decodeValue(final ByteBuffer byteBuffer) {

    final byte[] bytes = new byte[byteBuffer.remaining()]
    byteBuffer.duplicate().get(bytes)

    objectMapper.readValue(new String(bytes, Charsets.UTF_8), Place)
  }

  @Override
  ByteBuffer encodeKey(final String key) {

    Charsets.UTF_8.encode(key)
  }

  @Override
  ByteBuffer encodeValue(final Place place) {

    ByteBuffer.wrap(objectMapper.writeValueAsString(place).getBytes(Charsets.UTF_8))
  }
}
