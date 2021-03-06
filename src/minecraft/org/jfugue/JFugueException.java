/*
 * JFugue - API for Music Programming
 * Copyright (C) 2003-2008  David Koelle
 *
 * http://www.jfugue.org 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *  
 */

package org.jfugue;

/**
 * Handles JFugue parsing exceptions.
 *
 *@author David Koelle
 *@version 2.0
 */
public class JFugueException extends RuntimeException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Object[] objects = null;
	protected String format;
	
	/**
	 * @return the objects
	 */
	public Object[] getObjects() {
		return objects; // TODO make a copy
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	public JFugueException() {
		super();
	}
	
	/**
     * Create a new JFugueException.
     *
     * @param cause The cause of the exception.  
     */
    public JFugueException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Create a new JFugueException.
     *
     * @param format The format string representing the exception.  
     * @param objects the Objects to be formatted with the format string.
     */
    public JFugueException(String format, Object...objects)
    {
        super(String.format(format, objects));
        this.format = format;
        this.objects = objects;
    }
    
    /**
     * Create a new JFugueException.
     * 
     * @param cause The cause of the exception.
     * @param format The format string representing the exception.  
     * @param objects the Objects to be formatted with the format string.
     */
    public JFugueException(Throwable cause, String format, Object...objects) {
		super(format, cause);
		this.objects = objects;
	}
    
    public String toString() {
		if (objects == null)
			return super.toString();
		else
			return getClass().getSimpleName() + ": " + String.format(getLocalizedMessage(), objects);
	}

    /**
     * Create a new JFugueException.
     *
     * @param exc The string representing the exception.  This should contain the * character, so 'param' can be placed into the string.
     * @param token The token or dictionary entry in which the exception has been discovered
 	 * @deprecated
     */
    public JFugueException(String exc, String token)
    {
        super(exc + " Found while parsing the following token, word, or definition: "+token);
    }

    /**
     * Create a new JFugueException.
     *
     * @param exc The string representing the exception.  This should contain the * character, so 'param' can be placed into the string.
     * @param param The direct object of the exception, the thing that has had some problem with it
     * @param token The token or dictionary entry in which the exception has been discovered
 	 * @deprecated
     */
    public JFugueException(String exc, String param, String token)
    {
        super(exc.substring(0, exc.indexOf("%s")) + param + exc.substring(exc.indexOf("%s")+2, exc.length()) + " Found while parsing the following token, word, or definition: "+token);
    }

    /** The Voice command, V<i>voice</i>, is out of range. */
    public static final String VOICE_EXC = "Voice (%s) is not possible; use a number from 0-15.";
    /** The Tempo command, T<i>tempo</i>, is out of range. */
    public static final String TEMPO_EXC = "Tempo (%s) is not possible; use a number from 20 to 300 or a tempo name.";
    /** The KeySignature command, K<i>keysig</i>, is poorly formed. */
    public static final String KEYSIG_EXC = " %s is not a proper key signature; should be like KC#maj or KAbmin.";
    public static final String KEYSIG_SCALE_EXC = " %s is not a proper key signature scale name; should be either min or maj";
    /** The Layer command, L<i>layer</i>, is out of range. */
    public static final String LAYER_EXC = "Layer %s is not possible; use a number from 0-15.";
    /** The Instrument command, I<i>instrument</i>, is not a valid instrument. */
//    public static final String INSTRUMENT_EXC = "Instrument %s is not a valid instrument name, or is not in the range 0 - 127.";
    /** The index of the Controller command, X<i>index</i>=<i>value</i>, is not a valid controller. */
//    public static final String CONTROL_EXC = "Control %s is not a valid controller name, or is not in the range 0 - 127.";
    /** The Note command does not specify a valid percussion sound. */
//    public static final String NOTE_EXC = "Note %s is not a valid drum sound name, or is not in the range 0 - 127.";
    /** The Octave specifier within the Note command is out of range. */
//    public static final String OCTAVE_EXC = "Octave %s is not a number, or is not in the range 0 - 10.";
    /** The Octave value calculated by the parser is out of range. */
    public static final String NOTE_OCTAVE_EXC = "A note is too high or too low: The note value %s, calculated by computing (octave*12)+noteValue, is not in the range 0 - 127.";
    /** The Duration part of the MusicString has an error. */
    public static final String NOTE_DURATION_EXC = "The Duration part of the MusicString has an error.";
    /** The Velocity character is not known. */
    public static final String NOTE_VELOCITY_EXC = "The velocity character in %s is unknown.";
    /** The root note for a chord inversion has an error. */
    public static final String INVERSION_EXC = "The root given for a chord inversion is less than the initial chord root, or greater than the range of the chord.";

    /** The parser encountered spaces in a single token. */
//    public static final String PARSER_SPACES_EXC = "The token %s sent to Parser.parse() contains spaces.  A token is one unit of musical data, and should not contain a space.";
    /** The parser cannot find a definition for the given word. */
    public static final String WORD_NOT_DEFINED_EXC = "The word %s has no definition.  Check the spelling, or define the word before using it.  See the JFugue Instruction Manual for information on defining words.";
    /** The Controller command, X<i>index</i>=<i>value</i>, is malformed. */
    public static final String CONTROL_FORMAT_EXC = "The controller token %s is missing an equals sign.  See the JFugue Instruction Manual for information on using the Controller token.";
    /** The Sysex command, ^<i>[ dec | hex ]:byte,byte,byte,..., is malformed. */
    public static final String SYSEX_FORMAT_EXC = "The sysex token %s is malformed.  See the JFugue Instruction Manual for information on using the Sysex token.";

    /** The parser expected a byte. */
    public static final String EXPECTED_BYTE   = "MCDitty expected a byte (a number from 0-127) somewhere, but encountered the value (%s) which is not a byte.";
    /** The parser expected a long. */
    public static final String EXPECTED_LONG   = "MCDitty expected a number somewhere, but encountered the value (%s) which is not an Integer or is too extremely high/low."; // Long
    /** The parser expected an int. */
    public static final String EXPECTED_INT    = "MCDitty expected an number somewhere, but encountered the value (%s) which is not an integer or is too extremely high/low."; // Int
    /** The parser expected a double. */
    public static final String EXPECTED_DOUBLE = "MCDitty expected a number somewhere, but encountered the value (%s) which is not a 'double' (a type of number where a decimal point is allowed).";
    
    /** The MIDI System cannot instantiate a sequencer. */
    public static final String SEQUENCER_DEVICE_NOT_SUPPORTED_WITH_EXCEPTION = "The MIDI System cannot instantiate a sequencer.  Although this error is reported by JFugue, the problem is not with JFugue itself.  Find resources for using MIDI on your specific system.  The exception message from MidiSystem.getSequencer() is: ";
    /** The MIDI System cannot instantiate a sequencer. */
    public static final String SEQUENCER_DEVICE_NOT_SUPPORTED = "The MIDI System cannot instantiate a sequencer.  Although this error is reported by JFugue, the problem is not with JFugue itself.  Find resources for using MIDI on your specific system.";

    /** Player.play(String) plays a music string, not a filename */
    public static final String PLAYS_STRING_NOT_FILE_EXC = "play(String) plays a music string, not a filename.  Try using play(File).";

    /** Error playing music */
    public static final String ERROR_PLAYING_MUSIC = "Error playing music: ";

    /** Error while sleep */
    public static final String ERROR_SLEEP = "Error while sleeping";
    
    /** Error resolving MidiDevice with Intelligent Resolver */
    public static final String INTELLIGENT_RESOLVER_FAILED = "IntelligentDeviceResolver not intelligent enough.  Be explicit about the MidiDevice.";

    /** Verification exception */
    public static final String VERIFICATION_EXCEPTION = "The result of parsing, '%s', was not expected.";
    
    /** General error */
    public static final String GENERAL_ERROR = "General error: ";
    
//    public static final String PARSE_CHAR_ERROR = "The charactor '%s' was not expected while parsing %s";
    
    /** Error for MusicXMLParser (TODO: Should this reside in MusicXMLParser?) */
    public static final String BEAT_UNIT_MUST_BE_QUARTER = "MusicXML tag \"beat-unit\" must be set to \"quarter\"";
    
	public static final String TEMPO_LOW_RANGE_EXCEPTION = "The tempo %s is too slow; MCDitty can play at 20 BPM or faster.";
	public static final String TEMPO_HIGH_RANGE_EXCEPTION = "The tempo %s is too fast; MCDitty can play at up to 300 BPM.";
	
	public static final String EMPTY_LYRIC_EXCEPTION = "This lyrics token has no lyric name afterwards.";

}