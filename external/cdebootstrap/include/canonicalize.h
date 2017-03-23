#ifndef CANONICALIZE_H_
#define CANONICALIZE_H_

#define canonicalize_file_name(target) realpath(target, NULL)
#endif /* !CANONICALIZE_H_ */
