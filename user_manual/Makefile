PANDOC = pandoc

%.pdf: %.md Makefile
	$(PANDOC) -s -S --latex-engine xelatex \
	-N -V geometry:margin=1in \
	-V mainfont='Source Sans Pro' \
	-V fontsize='11pt' \
	--biblio bibliography.bib \
	-o $@ $<
 
all: manual.pdf
 
clean:
	rm -f *.{pdf,tex}
