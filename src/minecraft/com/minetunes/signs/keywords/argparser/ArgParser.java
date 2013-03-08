/**
 * Copyright (c) 2012-2013 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes.signs.keywords.argparser;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * A reusable parser for keyword arguments on a sign.
 * 
 */
public class ArgParser {

	/**
	 * A list of lines, each with a list of args.
	 */
	private LinkedList<LinkedList<Arg>> lines = new LinkedList<LinkedList<Arg>>();

	private LinkedList<ArgParseError> parseErrors = new LinkedList<ArgParseError>();

	public ArgParser() {

	}

	/**
	 * Parses the first line of a keyword
	 * 
	 * @param signLine
	 *            null okay
	 */
	public void parse(String signLine) {
		LinkedList<String> l = new LinkedList<String>();
		l.add(signLine);
		parse(l);
	}

	/**
	 * 
	 * @param signText
	 *            null okay; all four lines of a sign
	 * @param startLine
	 *            range checked; starts parsing from this line of the sign
	 *            (still being the first line of the keyword)
	 */
	public void parse(String[] signText, int startLine) {
		if (startLine >= signText.length) {
			return;
		}

		LinkedList<String> l = new LinkedList<String>();
		if (signText != null) {
			for (int i = startLine; i < signText.length; i++) {
				l.add(signText[i]);
			}
		}

		parse(l);
	}

	/**
	 * Parses the given lines, saving the parsed values. Clears any previous
	 * parse() results first.
	 * 
	 * @param signLines
	 *            null okay
	 */
	public void parse(LinkedList<String> signLines) {
		// Clear any previous parsings first
		clearParsedData();

		// Iterate over lines
		for (int currLine = 0; currLine < signLines.size(); currLine++) {
			// Tokenize sign line, after checking for a null element
			String signLineRaw = signLines.get(currLine);
			if (signLineRaw == null) {
				continue;
			}
			// Trim removes leading and following spaces; \s+ removes multiple
			// spaces between
			String[] tokensArr = signLineRaw.trim().split("\\s+");
			LinkedList<String> tokens = new LinkedList<String>(
					Arrays.asList(tokensArr));

			// If this is the first line, ignore the keyword itself
			if (currLine == 0) {
				tokens.pollFirst();
			}

			// Attempt to parse each argument of the line in order
			if (currLine < lines.size()) {
				for (Arg arg : lines.get(currLine)) {
					// Parse
					int readTokens = arg.parse(tokens);

					// Check for errors
					if (arg.hasFatalErrors()) {
						if (arg.isOptional()) {
							// Ignore and move on
							continue;
						}
					}

					// Note any errors
					parseErrors.addAll(arg.getParseErrors());

					// Remove tokens
					for (int i = 0; i < readTokens; i++) {
						tokens.pollFirst();
					}
				}

				// Check for unparsed, but non-optional, keywords
				for (Arg a : lines.get(currLine)) {
					if (!a.isParsed() && !a.isOptional()) {
						// Uh oh: missing arg!
						parseErrors.add(new ArgParseError(a, "is missing.",
								true));
					}
				}
			}

			// Check for excess tokens that went unused
			if (tokens.size() > 0) {
				parseErrors.add(new ArgParseError(null, llToString(tokens)
						+ " should be removed.", true));
			}
		}
	}

	/**
	 * Combines the list of strings into a single string with spaces between
	 * each list element.
	 * 
	 * @param l
	 * @return
	 */
	private String llToString(LinkedList<String> l) {
		StringBuilder sb = new StringBuilder();
		for (String s : l) {
			sb.append(" ").append(s);
		}
		return sb.toString().trim();
	}

	// /**
	// * Converts a String[] into a LinkedList<String>
	// *
	// * @param arr
	// * @return
	// */
	// private LinkedList<String> arrToLL(String[] arr) {
	// LinkedList ll = new LinkedList<String>();
	// if (arr != null) {
	// for (String s : arr) {
	// ll.add(s);
	// }
	// }
	// return ll;
	// }

	/**
	 * Removes the results of the last parse() from all args, while leaving the
	 * args intact.
	 */
	public void clearParsedData() {
		parseErrors.clear();
		for (LinkedList<Arg> line : lines) {
			for (Arg arg : line) {
				arg.reset();
			}
		}
	}

	/**
	 * 
	 * @param lineArgs
	 * @return this parser, for chaining
	 */
	public ArgParser addLine(Arg... line) {
		LinkedList<Arg> lineArgs = new LinkedList<Arg>();
		if (lineArgs != null) {
			for (Arg a : line) {
				lineArgs.add(a);
			}
		}
		lines.add(lineArgs);

		return this;
	}

	/**
	 * 
	 * @return a list, possibly empty, of errors
	 */
	public LinkedList<ArgParseError> getParseErrors() {
		return (LinkedList<ArgParseError>) parseErrors.clone();
	}

}
