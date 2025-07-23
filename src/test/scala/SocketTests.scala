package it.unibo.scafi.runtime.network.sockets

import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.time.{ Seconds, Span }

import java.net.ServerSocket
import java.util.concurrent.{ CountDownLatch, ForkJoinPool, TimeUnit }
import scala.concurrent.*

class SocketTests extends AnyFlatSpec with should.Matchers with ScalaFutures with Eventually:

  given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool())

  override given patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(60, Seconds)))

  private val FreePort = 0

  "when blocked on accept and the server is closed" should "throw a SocketException" in:
    for _ <- 0 until 10 do
      val acceptStarted = new CountDownLatch(1)
      val serverSocket = new ServerSocket(FreePort)
      val serverFuture = Future:
        acceptStarted.countDown()
        serverSocket.accept()
      val started = acceptStarted.await(5, TimeUnit.SECONDS) // wait until accept is about to be called
      if !started then fail("Server socket accept did not start after 5 seconds!")
      Thread.sleep(1_000) // give some time for the accept to block
      serverFuture.isCompleted shouldBe false
      serverSocket.close()
      eventually(serverSocket.isClosed shouldBe true)
      eventually(serverFuture.failed.futureValue shouldBe a[java.net.SocketException])
end SocketTests
