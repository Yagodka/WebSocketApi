package com.websocket.util

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.ContentTypeRange
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import upickle.default.{readJs, writeJs, Reader, Writer}
import upickle.{json, Js}
import scala.collection.immutable.Seq

object UpickleSupport extends UpickleSupport

trait UpickleSupport {

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    List(`application/json`)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset) => data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller = Marshaller.stringMarshaller(`application/json`)

  implicit def unmarshaller[A: Reader]: FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(data => readJs[A](json.read(data)))

  implicit def marshaller[A: Writer]: ToEntityMarshaller[A] =
    jsonStringMarshaller.compose(json.write(_: Js.Value, 0)).compose(writeJs[A])
}