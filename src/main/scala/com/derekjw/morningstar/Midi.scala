package com.derekjw.morningstar

import zio.*

import javax.sound.midi.{MidiDevice, MidiMessage, MidiSystem, ShortMessage, Receiver}

trait Midi:
  def send(midiCommand: Midi.Command): Task[Unit]


object Midi:
  sealed trait Command
  case class ProgramChange(channel: Int, program: Int) extends Command
  case class ControlChange(channel: Int, control: Int, value: Int) extends Command

  def send(midiCommand: Midi.Command): RIO[Midi, Unit] = ZIO.serviceWithZIO[Midi](_.send(midiCommand))

  def live(midiDevice: Device): Layer[Throwable, Midi] = midiDevice.receiver.map(MidiLive.apply).toLayer

  def live(selector: MidiDevice.Info => Boolean): Layer[Throwable, Midi] = selectDevice(selector).toLayer.flatMap(d => live(d.get))

  case class Device(private val underlying: MidiDevice):
    val close: Task[Unit] = ZIO(underlying.close())
    val receiver: Managed[Throwable, Receiver] =
      ZManaged.acquireReleaseWith(ZIO(underlying.getReceiver))(r => ZIO(r.close()).orDie)

  def getDeviceInfos: Task[List[MidiDevice.Info]] = ZIO(MidiSystem.getMidiDeviceInfo.toList)

  def getDevice(deviceInfo: MidiDevice.Info): Managed[Throwable, Device] =
    val acquire = ZIO {
      val device = MidiSystem.getMidiDevice(deviceInfo)
      device.open()
      Device(device)
    }
    ZManaged.acquireReleaseWith(acquire)(_.close.orDie)

  def selectDevice(selector: MidiDevice.Info => Boolean): Managed[Throwable, Device] =
    getDeviceInfos
      .flatMap(infos => ZIO.fromOption(infos.find(selector)).orDieWith(_ => new RuntimeException("Unable to find midi device")))
      .toManaged
      .flatMap(getDevice)

  case class MidiLive(private val receiver: Receiver) extends Midi:

    override def send(midiCommand: Midi.Command): Task[Unit] =
      ZIO {
        val midiMessage =
          midiCommand match
            case ProgramChange(channel, program) => new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0)
            case ControlChange(channel, control, value) => new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, control, value)

        receiver.send(midiMessage, -1)
      } <* ZIO.logInfo(s"Sending $midiCommand")

