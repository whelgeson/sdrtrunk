/*******************************************************************************
 * sdrtrunk
 * Copyright (C) 2014-2017 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 ******************************************************************************/
package source.tuner.limesdr;

import org.anhonesteffort.uhd.Device;
import org.anhonesteffort.uhd.RxStreamer;
import org.anhonesteffort.uhd.StreamArgs;
import org.anhonesteffort.uhd.types.*;
import org.anhonesteffort.uhd.usrp.MultiUsrp;
import org.anhonesteffort.uhd.util.ComplexFloatVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.Listener;
import sample.complex.ComplexBuffer;
import source.SourceException;
import source.tuner.TunerController;
import source.tuner.configuration.TunerConfiguration;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by billh on 3/10/17.
 */
public class LimeSDRTunerController extends TunerController {
    private final static Logger logger = LoggerFactory.getLogger(LimeSDRTunerController.class);

    private static final long MINIMUM_FREQUENCY = 1000000l;
    private static final long MAXIMUM_FREQUENCY = 3800000000l;
    private static final int DC_SPIKE_BUFFER = 5000;
    private static final double USEABLE_BANDWIDTH_RATIO = 0.95;
    private MultiUsrp multiUsrp;
    private DeviceAddress deviceAddress;
    private String serialNumber;
    private long bufferSize = 10000;

    private final List<Listener<ComplexBuffer>> sampleListeners = Collections.synchronizedList(new ArrayList());
    private LinkedTransferQueue<float[]> producedSamples = new LinkedTransferQueue<>();
    private SamplesProducer samplesProducer;

    public LimeSDRTunerController() {
        super(MINIMUM_FREQUENCY, MAXIMUM_FREQUENCY, DC_SPIKE_BUFFER, USEABLE_BANDWIDTH_RATIO);
    }

    public void initDevices() throws SourceException {
        logger.debug("Initalizing LimeSDR Device");
        try{
            DeviceAddresses addresses = Device.find(new DeviceAddress("Lime"));
            if (addresses.size() < 1) {
                throw new SourceException("No LimeSDR Devices Found");
            }
            deviceAddress = addresses.get(0);
            multiUsrp = MultiUsrp.build(deviceAddress);
            long numMBoards = multiUsrp.get_num_mboards();
            long numRxChans = multiUsrp.get_rx_num_channels();

            if (numMBoards != 1 || numRxChans < 1) {
                throw new SourceException(String.format(
                        "Unable to initialize LimeSDR Device. MotherBoards Found: %d  RxChannels Found: %d", numMBoards,
                        numRxChans));
            }
            logger.debug("LimeSDR DeviceAddress: " + deviceAddress.to_pp_string());
            for (String s : deviceAddress.to_pp_string().split(System.lineSeparator())) {
                if (s.contains("serial")) {
                    serialNumber = s.substring(s.indexOf(":") + 1, s.length()).trim();
                }
            }
            setFrequency(MINIMUM_FREQUENCY);
        } catch (RuntimeException e) {
            throw new SourceException("Unable to initialize LimeSDR Device", e);
        }
    }

    @Override
    public String toString() {
        return deviceAddress.to_pp_string();
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public void apply(TunerConfiguration config) throws SourceException {
        logger.debug("Applying TunerConfig for LimeSDR Device");
        if (!(config instanceof LimeSDRTunerConfiguration)) {
            throw new IllegalArgumentException("Illegal TunerConfigurationType " + config.getClass());
        }
        LimeSDRTunerConfiguration limeConfig = (LimeSDRTunerConfiguration) config;

        try {
            multiUsrp.set_clock_source(limeConfig.getClockSource(), MultiUsrp.ALL_MBOARDS);
            multiUsrp.set_rx_antenna(limeConfig.getRxAntenna(), 0);
            mFrequencyController.setSampleRate(Math.toIntExact(limeConfig.getRxSampleRate()));
            multiUsrp.set_rx_rate(mFrequencyController.getBandwidth());
            multiUsrp.set_rx_gain(limeConfig.getRxGainLNA(), "LNA", 0);
            multiUsrp.set_rx_gain(limeConfig.getRxGainLNA(), "PGA", 0);
            multiUsrp.set_rx_gain(limeConfig.getRxGainLNA(), "TIA", 0);
            bufferSize = limeConfig.getRxBufferSize();
            setFrequency(getFrequency());
        } catch (RuntimeException e) {
            throw new SourceException("Unable to Configure LimeSDR Device", e);
        }

    }

    public void setBufferSize(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setClockSource(String source) throws SourceException {
        try {
            multiUsrp.set_clock_source(source, MultiUsrp.ALL_MBOARDS);
        } catch (RuntimeException e) {
            throw new SourceException(
                    "Unable set Antenna Selection for LimeSDR Device " + deviceAddress.to_string(), e);
        }
    }

    public void setAntenna(String antenna) throws SourceException {
        try {
            multiUsrp.set_rx_antenna(antenna, 0);
        } catch (RuntimeException e) {
            throw new SourceException(
                    "Unable set Antenna Selection for LimeSDR Device " + deviceAddress.to_string(), e);
        }
    }

    public void setLNAGain(int gain) throws SourceException {
        try {
            multiUsrp.set_rx_gain(gain, "LNA", 0);
        } catch (RuntimeException e) {
            throw new SourceException("Unable set LNA Gain for LimeSDR Device " + deviceAddress.to_string(), e);
        }
    }

    public void setPGAGain(int gain) throws SourceException {
        try {
            multiUsrp.set_rx_gain(gain, "PGA", 0);
        } catch (RuntimeException e) {
            throw new SourceException("Unable set PGA Gain for LimeSDR Device " + deviceAddress.to_string(), e);
        }
    }

    public void setTIAGain(int gain) throws SourceException {
        try {
            multiUsrp.set_rx_gain(gain, "TIA", 0);
        } catch (RuntimeException e) {
            throw new SourceException("Unable set TIA Gain for LimeSDR Device " + deviceAddress.to_string(), e);
        }
    }

    public void setSampleRate(long rate) throws SourceException {
        try {
            multiUsrp.set_rx_rate(rate);
            mFrequencyController.setSampleRate(Math.toIntExact(rate));
        } catch (RuntimeException e) {
            throw new SourceException("Unable to set sample rate for LimeSDR Device " + deviceAddress.to_string(), e);
        }
    }

    @Override
    public long getTunedFrequency() throws SourceException {
        try {
            return (long) multiUsrp.get_rx_freq(0);
        } catch (RuntimeException e) {
            throw new SourceException("Unable to get Tuned Frequency from LimeSDR Device", e);
        }
    }

    @Override
    public void setTunedFrequency(long frequency) throws SourceException {
        try {
            TuneRequest request = new TuneRequest(frequency);
            TuneResult result = multiUsrp.set_rx_freq(request, 0);
            logger.debug("Tune Frequency Set to " + result.actual_rf_freq());
        } catch (RuntimeException e) {
            throw new SourceException("Unable to tune frequency on LimeSDR Device", e);
        }
    }

    @Override
    public int getCurrentSampleRate() throws SourceException {
        try {
            return (int) multiUsrp.get_rx_rate(0);
        } catch (RuntimeException e) {
            throw new SourceException("Unable to get Tuned Frequency from LimeSDR Device", e);
        }
    }


    protected synchronized void addListener(Listener<ComplexBuffer> listener) {
        sampleListeners.add(listener);
        if (samplesProducer == null || samplesProducer.isStopped()) {
            samplesProducer = new SamplesProducer();
            Thread thread = new Thread(samplesProducer);

            thread.setDaemon(true);
            thread.setName(SamplesProducer.class.getSimpleName());
            thread.start();
        }
    }

    protected synchronized void removeListener(Listener<ComplexBuffer> listener) {
        sampleListeners.remove(listener);

        if (sampleListeners.isEmpty()) {
            samplesProducer.stopReading();
        }
    }


    private void broadcastSamples(ComplexBuffer samples) {
        sampleListeners.parallelStream().forEach(e-> e.receive(samples));
    }

    private class SamplesProducer implements Runnable {

        private RxStreamer rxStreamer;
        private boolean stopped = false;

        private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        private ScheduledFuture<?> samplesConsumerTask;

        public SamplesProducer() {
            rxStreamer = multiUsrp.getRxStream(new StreamArgs("fc32", "sc16"));
        }

        public boolean isStopped() {
            return stopped;
        }

        private void startReading() {
            multiUsrp.issue_stream_cmd(
                    new StreamCommand(StreamCommand.START_CONTINUOUS),
                    MultiUsrp.ALL_CHANS
            );

            executor.execute(new SamplesConsumer());
            //samplesConsumerTask = executor.scheduleAtFixedRate(new SamplesConsumer(), 0, 10, TimeUnit.MILLISECONDS);
            stopped = false;
        }

        public void stopReading() {
            multiUsrp.issue_stream_cmd(
                    new StreamCommand(StreamCommand.STOP_CONTINUOUS),
                    MultiUsrp.ALL_CHANS
            );
            if (samplesConsumerTask != null) {
                samplesConsumerTask.cancel(true);
            }
            producedSamples.clear();
            stopped = true;
        }

        @Override
        public void run() {
            int errorCount = 0;
            ComplexFloatVector samplesVector = new ComplexFloatVector(bufferSize);
            RxMetadata rxMetadata = new RxMetadata();
            startReading();
            while (!stopped) {
                rxStreamer.recv(samplesVector.front(),
                        samplesVector.size(),
                        rxMetadata, 1, false);

                if (rxMetadata.error_code() == RxMetadata.ERROR_OVERFLOW) {
                    logger.warn("OVERFLOW! D:");
                } else if (rxMetadata.error_code() != RxMetadata.ERROR_NONE) {
                    errorCount++;
                    stopReading();
                    if (errorCount < 5) {
                        startReading();
                        logger.error("read returned error " + rxMetadata.error_code() + ", restarting.");
                    } else {
                        logger.error("read returned error " + rxMetadata.error_code() + ", stopping.");
                    }
                } else {
                    errorCount = 0;
                    FloatBuffer fb = samplesVector.toFloatBuffer();
                    float[] fl = new float[fb.capacity()];
                    fb.get(fl);
                    producedSamples.add(fl);
                    fl = null;
                }
            }
        }
    }

    private class SamplesConsumer implements Runnable {
        @Override
        public void run() {
            while(true){
                try {
                    broadcastSamples(new ComplexBuffer(producedSamples.take()));
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


}