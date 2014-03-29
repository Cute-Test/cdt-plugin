PANDOC = pandoc

%.pdf: %.md Makefile
	$(PANDOC) --smart --standalone --latex-engine xelatex \
	--toc --number-sections \
	--variable geometry:margin=1in \
	--variable mainfont='Source Sans Pro' \
	--variable fontsize='11pt' \
	--biblio bibliography.bib \
	-o $@ $<
 
all: manual.pdf
 
clean:
	rm -f *.{pdf,tex}
