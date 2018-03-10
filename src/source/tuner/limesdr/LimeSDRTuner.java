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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.Listener;
import sample.complex.ComplexBuffer;
import source.SourceException;
import source.tuner.*;

import java.util.concurrent.RejectedExecutionException;

/**
 * Created by billh on 3/7/17.
 */
public class LimeSDRTuner extends Tuner {

    private final static Logger logger = LoggerFactory.getLogger(LimeSDRTuner.class);

    public LimeSDRTuner(LimeSDRTunerController controller) throws SourceException {
        super("LimeSDR", controller);
    }

    public LimeSDRTunerController getController() {
        return (LimeSDRTunerController) getTunerController();
    }

    @Override
    public TunerClass getTunerClass() {
        return TunerClass.LIME_SDR;
    }

    @Override
    public TunerType getTunerType() {
        return TunerType.LIMESDR;
    }

    @Override
    public double getSampleSize() {
        return 32.0;
    }

    @Override
    public TunerChannelSource getChannel(TunerChannel channel)
            throws RejectedExecutionException, SourceException {
        TunerChannelSource source = getController().getChannel(this, channel);

        if (source != null) {
            broadcast(new TunerEvent(this, TunerEvent.Event.CHANNEL_COUNT));
        }
        return source;
    }

    @Override
    public void releaseChannel(TunerChannelSource source) {
        /* Unregister for receiving samples */
        removeListener(source);

		/* Tell the controller to release the channel and cleanup */
        if (source != null) {
            getController().releaseChannel(source);
        }
    }

    @Override
    public String getUniqueID() {
        String serial = "";
        try {
            serial = ((LimeSDRTunerController) super.getTunerController()).getSerialNumber();
        } catch (Exception e) {
            logger.error("Error while trying to get LimeSDR serial number", e);
        }
        return serial;
    }

    @Override
    public void addListener(Listener<ComplexBuffer> listener) {
        getController().addListener(listener);
    }

    @Override
    public void removeListener(Listener<ComplexBuffer> listener) {
        getController().removeListener(listener);
    }
}