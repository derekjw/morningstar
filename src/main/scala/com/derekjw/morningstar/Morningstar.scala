package com.derekjw.morningstar

import zio.*

trait Morningstar:
  def bankUp: Task[Unit]
  def bankDown: Task[Unit]


// sysex docs: https://morningstarengineering.atlassian.net/wiki/spaces/MMS/pages/918519809/SysEx+Documentation+for+External+Applications
object Morningstar:
  val bankUp: RIO[Morningstar, Unit] = ZIO.serviceWithZIO[Morningstar](_.bankUp)
  val bankDown: RIO[Morningstar, Unit] = ZIO.serviceWithZIO[Morningstar](_.bankDown)

  def live(channel: Int): URLayer[Midi, Morningstar] = (MorningstarLive(channel, _)).toLayer

  case class MorningstarLive(channel: Int, midi: Midi) extends Morningstar:
    def bankUp: Task[Unit] = midi.send(Midi.ControlChange(channel, 0, 0))
    def bankDown: Task[Unit] = midi.send(Midi.ControlChange(channel, 1, 0))

