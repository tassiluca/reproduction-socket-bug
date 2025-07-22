package it.unibo.scafi.runtime.network.sockets

import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.time.Seconds
import org.scalatest.time.Span

import java.net.ServerSocket
import java.util.concurrent.ForkJoinPool
import scala.concurrent.*

class SocketTests extends AnyFlatSpec with should.Matchers with ScalaFutures with Eventually:

  given ExecutionContext = ExecutionContext.fromExecutor(ForkJoinPool())

  override given patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)))

  "when blocked on accept and the server is closed" should "throw an exception" in:
    for i <- 0 until 10 do
      println("Run " + i)
      val freePort = 0
      val startedPromise = Promise[Unit]()
      val serverSocket = new ServerSocket(freePort)
      println(">>>>> Starting server socket <<<<<")
      val serverFuture = Future:
        println(">>>>> Accepting connection <<<<<")
        startedPromise.success(())
        serverSocket.accept()
        println("<<<< Effectively closed <<<<")
      startedPromise.future.futureValue
      Thread.sleep(1_000)
      println(">>>>> Closing server socket <<<<<")
      serverSocket.close()
      whenReady(serverFuture.failed):
        _ shouldBe a[java.net.SocketException]
end SocketTests
